package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.CoupleInviteDTO;
import com.basebackend.wheel.entity.CoupleRelationship;

/**
 * 情侣服务接口
 *
 * @author wheel-api
 * @since 2025-12-16
 */
public interface CoupleService {

    /**
     * 邀请情侣
     *
     * @param userId 当前用户ID
     * @param inviteDTO 邀请信息
     * @return 邀请结果
     */
    InviteResult inviteCouple(Long userId, CoupleInviteDTO inviteDTO);

    /**
     * 接受情侣邀请
     *
     * @param userId 当前用户ID
     * @param inviteCode 邀请码
     * @return 接受结果
     */
    AcceptResult acceptInvite(Long userId, String inviteCode);

    /**
     * 获取情侣关系状态
     *
     * @param userId 用户ID
     * @return 关系状态
     */
    CoupleStatus getCoupleStatus(Long userId);

    /**
     * 解除情侣关系
     *
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean unbindCouple(Long userId);

    /**
     * 获取用户的情侣信息
     *
     * @param userId 用户ID
     * @return 情侣信息
     */
    CoupleInfo getCoupleInfo(Long userId);

    /**
     * 验证邀请码是否有效
     *
     * @param inviteCode 邀请码
     * @return 是否有效
     */
    boolean validateInviteCode(String inviteCode);

    /**
     * 邀请结果
     */
    class InviteResult {
        private String inviteCode;
        private String partnerPhoneNumber;
        private String inviteMessage;
        private String status;

        // Getters and Setters
        public String getInviteCode() {
            return inviteCode;
        }

        public void setInviteCode(String inviteCode) {
            this.inviteCode = inviteCode;
        }

        public String getPartnerPhoneNumber() {
            return partnerPhoneNumber;
        }

        public void setPartnerPhoneNumber(String partnerPhoneNumber) {
            this.partnerPhoneNumber = partnerPhoneNumber;
        }

        public String getInviteMessage() {
            return inviteMessage;
        }

        public void setInviteMessage(String inviteMessage) {
            this.inviteMessage = inviteMessage;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 接受邀请结果
     */
    class AcceptResult {
        private Long relationshipId;
        private String partnerNickname;
        private String status;
        private String confirmedAt;

        // Getters and Setters
        public Long getRelationshipId() {
            return relationshipId;
        }

        public void setRelationshipId(Long relationshipId) {
            this.relationshipId = relationshipId;
        }

        public String getPartnerNickname() {
            return partnerNickname;
        }

        public void setPartnerNickname(String partnerNickname) {
            this.partnerNickname = partnerNickname;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getConfirmedAt() {
            return confirmedAt;
        }

        public void setConfirmedAt(String confirmedAt) {
            this.confirmedAt = confirmedAt;
        }
    }

    /**
     * 情侣状态
     */
    class CoupleStatus {
        private Integer status; // 0-无关系，1-待确认，2-已确认，3-已解除
        private String statusText;
        private Long partnerUserId;
        private String partnerNickname;
        private String inviteCode;
        private String createdAt;

        // Getters and Setters
        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getStatusText() {
            return statusText;
        }

        public void setStatusText(String statusText) {
            this.statusText = statusText;
        }

        public Long getPartnerUserId() {
            return partnerUserId;
        }

        public void setPartnerUserId(Long partnerUserId) {
            this.partnerUserId = partnerUserId;
        }

        public String getPartnerNickname() {
            return partnerNickname;
        }

        public void setPartnerNickname(String partnerNickname) {
            this.partnerNickname = partnerNickname;
        }

        public String getInviteCode() {
            return inviteCode;
        }

        public void setInviteCode(String inviteCode) {
            this.inviteCode = inviteCode;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }

    /**
     * 情侣详细信息
     */
    class CoupleInfo {
        private Long userId1;
        private String nickname1;
        private String avatarUrl1;
        private Long userId2;
        private String nickname2;
        private String avatarUrl2;
        private String status;
        private String confirmedAt;
        private String createdAt;

        // Getters and Setters
        public Long getUserId1() {
            return userId1;
        }

        public void setUserId1(Long userId1) {
            this.userId1 = userId1;
        }

        public String getNickname1() {
            return nickname1;
        }

        public void setNickname1(String nickname1) {
            this.nickname1 = nickname1;
        }

        public String getAvatarUrl1() {
            return avatarUrl1;
        }

        public void setAvatarUrl1(String avatarUrl1) {
            this.avatarUrl1 = avatarUrl1;
        }

        public Long getUserId2() {
            return userId2;
        }

        public void setUserId2(Long userId2) {
            this.userId2 = userId2;
        }

        public String getNickname2() {
            return nickname2;
        }

        public void setNickname2(String nickname2) {
            this.nickname2 = nickname2;
        }

        public String getAvatarUrl2() {
            return avatarUrl2;
        }

        public void setAvatarUrl2(String avatarUrl2) {
            this.avatarUrl2 = avatarUrl2;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getConfirmedAt() {
            return confirmedAt;
        }

        public void setConfirmedAt(String confirmedAt) {
            this.confirmedAt = confirmedAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
    }
}
