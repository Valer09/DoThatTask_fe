package homeaq.dothattask.dothattask_fe.dothattask_fe.Network

import io.ktor.client.request.HttpRequestBuilder

const val GROUP_ID_HEADER = "X-Group-Id"

/**
 * Single point of truth for setting the group context on a group-scoped
 * request. Replaces any previously-set value (so callers never end up with
 * duplicate `X-Group-Id` headers if a default ever sneaks one in).
 */
fun HttpRequestBuilder.withGroup(groupId: Int) {
    headers.remove(GROUP_ID_HEADER)
    headers.append(GROUP_ID_HEADER, groupId.toString())
}
