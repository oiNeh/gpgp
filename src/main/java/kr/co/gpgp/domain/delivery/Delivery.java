package kr.co.gpgp.domain.delivery;

import static javax.persistence.EnumType.STRING;
import static kr.co.gpgp.domain.delivery.DeliveryStatus.*;
import static lombok.AccessLevel.PROTECTED;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import kr.co.gpgp.domain.address.Address;
import kr.co.gpgp.domain.requirement.Requirement;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "requirement_id")
    private Requirement requirement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Enumerated(STRING)
    private DeliveryStatus status;

    private Delivery(Requirement requirement, Address address) {
        this.requirement = requirement;
        this.address = address;
        this.status = DeliveryStatus.init(); // 배송 상태 초기화
    }

    public static Delivery of(Requirement requirement, Address address) {
        return new Delivery(requirement, address);
    }


    /** 1 -> 2 */
    public void nextStepInstruct() {
        if (status != ACCEPT) {
            throw new IllegalArgumentException("변경하려는 이전 상태가 아니라 다음 상태로 갈 수 없습니다.");
        }
        status = DeliveryStatus.sequence.get(status);
    }
    /** 2 -> 3 */
    public void nextStepDeparture() {
        if (status != INSTRUCT) {
            throw new IllegalArgumentException("변경하려는 이전 상태가 아니라 다음 상태로 갈 수 없습니다.");
        }
        status = sequence.get(status);
    }


    /** 3 -> 4 */
    public void nextStepFinalDelivery() {
        if (status != IN_TRANSIT) {
            throw new IllegalArgumentException("변경하려는 이전 상태가 아니라 다음 상태로 갈 수 없습니다.");
        }
        status = sequence.get(status);
    }

    /** 5 -> 6 */
    public void nextStepInTransit() {
        if (status != DEPARTURE) {
            throw new IllegalArgumentException("변경하려는 이전 상태가 아니라 다음 상태로 갈 수 없습니다.");
        }
        status = sequence.get(status);
    }


    /** 5 -> 6 */
    public void nextStepPurchaseConfirmation() {
        if (status != FINAL_DELIVERY) {
            throw new IllegalArgumentException("변경하려는 이전 상태가 아니라 다음 상태로 갈 수 없습니다.");
        }
        status = sequence.get(status);
    }

    public void cancle() {
        if(status == WITHDRAW_ORDER)
            throw new IllegalArgumentException("이미 취소한 배송입니다.");
        status = status.cancelStatus();
    }


    public Long getUserId() {
        return getAddress().getUser().getId();
    }

    public String getStatusMessage() {
        return status.getMessage();
    }

    public String getRequirementMessage() {
        return getRequirement().getMessage();
    }

    public String getAddressRoadName() {
        return getAddress().getRoadName();
    }

    public String getAddressZipCode() {
        return getAddress().getZipCode();
    }

    public String getAddressName() {
        return getAddress().getName();
    }

    public String getAddressDetailed() {
        return getAddress().getDetailed();
    }


    public boolean isAccept() {
        return getStatus() == ACCEPT;
    }

    public boolean isInstruct() {
        return getStatus() == DeliveryStatus.INSTRUCT;
    }

    public boolean isDeparture() {
        return getStatus() == DeliveryStatus.DEPARTURE;
    }

    public boolean isInTransit() {
        return getStatus() == DeliveryStatus.IN_TRANSIT;
    }

    public boolean isFinalDelivery() {
        return getStatus() == DeliveryStatus.FINAL_DELIVERY;
    }


}
