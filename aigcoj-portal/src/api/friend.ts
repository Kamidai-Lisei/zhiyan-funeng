import { http, unwrap } from './http'

export interface FriendItem {
  id: number
  userName: string
  userAccount?: string
  userAvatar?: string
  lastMessage?: string
  lastTime?: string
  unread?: number
}

export interface FriendRequest {
  requestId: number
  fromId: number
  userName?: string
  userAvatar?: string
  userProfile?: string
  createTime?: string
}

export interface ChatMsg {
  id: number
  fromId: number
  toId: number
  content: string
  mine: boolean
  createTime: string
}

export interface SearchUser {
  id: number
  userName: string
  userAccount: string
  userAvatar?: string
  userProfile?: string
  status: 'none' | 'friends' | 'sent' | 'received'
}

export const friendApi = {
  list: () => unwrap<FriendItem[]>(http.get('/friend/list')),

  requests: () => unwrap<FriendRequest[]>(http.get('/friend/requests')),

  request: (friendId: number | string) => unwrap<boolean>(http.post('/friend/request', { friendId })),

  accept: (requestId: number | string) => unwrap<boolean>(http.post('/friend/accept', { requestId })),

  remove: (friendId: number | string) => unwrap<boolean>(http.post('/friend/remove', { friendId })),

  search: (name: string) => unwrap<SearchUser[]>(http.get('/friend/search', { params: { name } })),

  send: (toId: number | string, content: string) => unwrap<boolean>(http.post('/friend/send', { toId, content })),

  messages: (friendId: number | string, afterId = 0) =>
    unwrap<{ messages: ChatMsg[]; friend?: { id: number; userName: string; userAvatar?: string } }>(
      http.get('/friend/messages', { params: { friendId, afterId } }),
    ),
}
