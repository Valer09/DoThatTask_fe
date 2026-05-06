package homeaq.dothattask.dothattask_fe.dothattask_fe.Model.group

import kotlinx.serialization.Serializable

@Serializable
enum class GroupRole { MEMBER, ADMIN }

@Serializable
data class Group(
    val id: Int,
    val name: String,
    val ownerEmail: String,
    val color: String = "#7E57C2",
)

@Serializable
data class GroupMember(
    val username: String?,
    val email: String,
    val name: String,
    val role: GroupRole,
)

@Serializable
data class GroupInfo(
    val id: Int,
    val name: String,
    val ownerEmail: String,
    val color: String = "#7E57C2",
    val members: List<GroupMember>,
)

@Serializable
data class GroupSummary(
    val id: Int,
    val name: String,
    val color: String = "#7E57C2",
)

@Serializable
data class CreateGroupRequest(val name: String)

@Serializable
data class SendInviteRequest(val inviteeEmail: String)

@Serializable
enum class InviteStatus { PENDING, ACCEPTED, REJECTED, REVOKED }

@Serializable
data class Invite(
    val id: Int,
    val groupId: Int,
    val groupName: String,
    val groupColor: String = "#7E57C2",
    val inviterEmail: String,
    val inviteeEmail: String,
    val status: InviteStatus,
)
