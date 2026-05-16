import { useCallback, useEffect, useState } from 'react'
import api from '../api/axios'

export function useUsers() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchUsers = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/users')
      setUsers(res.data?.data ?? res.data ?? [])
    } catch {
      setError('Không thể tải danh sách người dùng.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchUsers()
  }, [fetchUsers])

  const updateRole = useCallback(
    async (id, role) => {
      const res = await api.patch(`/api/users/${id}/role`, { role })
      await fetchUsers()
      return res.data?.data ?? res.data
    },
    [fetchUsers]
  )

  return { users, loading, error, refetch: fetchUsers, updateRole }
}
