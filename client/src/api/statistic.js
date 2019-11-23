import request from '@/utils/request'

export function fetchList(query) {
  return request({
    url: '/getServerOnlineCount',
    method: 'post',
    params: query
  })
}

export function fetchGoldEggList(query) {
  return request({
    url: '/getDailyRoomJinDanChangeData',
    method: 'post',
    params: query
  })
}
