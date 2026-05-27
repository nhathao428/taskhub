import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import api from '../api/axios'

describe('axios instance — JWT interceptor', () => {
  let originalLocation

  beforeEach(() => {
    // Stub window.location vì 401 handler gán window.location.href = '/login'.
    // Nếu không stub, jsdom mutate location thật → test sau có thể thấy state bị "ô nhiễm".
    // (localStorage clear đã có ở setup.js afterEach toàn cục.)
    originalLocation = window.location
    delete window.location
    window.location = { href: '' }
  })

  afterEach(() => {
    window.location = originalLocation
  })

  it('attaches Bearer token from localStorage to outgoing requests', async () => {
    localStorage.setItem('token', 'test-jwt-abc')
    const config = await api.interceptors.request.handlers[0].fulfilled({
      headers: {},
      url: '/api/anything',
    })
    expect(config.headers.Authorization).toBe('Bearer test-jwt-abc')
  })

  it('does not attach Authorization header when no token stored', async () => {
    const config = await api.interceptors.request.handlers[0].fulfilled({
      headers: {},
      url: '/api/anything',
    })
    expect(config.headers.Authorization).toBeUndefined()
  })

  it('clears token + user on 401 response and redirects to /login', async () => {
    localStorage.setItem('token', 'soon-to-be-expired')
    localStorage.setItem('user', JSON.stringify({ username: 'x' }))

    const onError = api.interceptors.response.handlers[0].rejected
    await expect(
      onError({ response: { status: 401 } })
    ).rejects.toBeDefined()

    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('user')).toBeNull()
    expect(window.location.href).toBe('/login')
  })

  it('passes through non-401 errors without clearing token', async () => {
    localStorage.setItem('token', 'still-valid')

    const onError = api.interceptors.response.handlers[0].rejected
    await expect(
      onError({ response: { status: 500 } })
    ).rejects.toBeDefined()

    expect(localStorage.getItem('token')).toBe('still-valid')
    expect(window.location.href).toBe('')
  })
})

describe('axios instance — demo mode adapter', () => {
  afterEach(() => {
    sessionStorage.clear()
  })

  it('routes non-auth requests to demoApiResponse when demo mode is on', async () => {
    sessionStorage.setItem('demo', '1')

    const response = await api.get('/api/tasks')
    // Demo data is canned — must return a successful axios-shaped response.
    expect(response.status).toBe(200)
    expect(response.data).toBeDefined()
  })

  it('blocks non-GET requests in demo mode with isDemoBlock', async () => {
    sessionStorage.setItem('demo', '1')

    // POST/PUT/DELETE trên demo → reject với isDemoBlock=true (yêu cầu đăng nhập).
    await expect(api.post('/api/tasks', { title: 'demo' }))
      .rejects.toMatchObject({ isDemoBlock: true, response: { status: 403 } })
  })
})
