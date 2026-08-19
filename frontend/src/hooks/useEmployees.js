import { useCallback, useEffect, useState } from 'react'
import api from '../api/axios'

export function useEmployees() {
  const [employees, setEmployees] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchEmployees = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/employees')
      setEmployees(res.data?.data ?? res.data ?? [])
    } catch {
      setError('Không thể tải danh sách nhân viên.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchEmployees() }, [fetchEmployees])

  const createEmployee = useCallback(async (data) => {
    const res = await api.post('/api/employees', data)
    await fetchEmployees()
    return res.data?.data ?? res.data
  }, [fetchEmployees])

  const updateEmployee = useCallback(async (id, data) => {
    const res = await api.put(`/api/employees/${id}`, data)
    await fetchEmployees()
    return res.data?.data ?? res.data
  }, [fetchEmployees])

  const deleteEmployee = useCallback(async (id) => {
    await api.delete(`/api/employees/${id}`)
    await fetchEmployees()
  }, [fetchEmployees])

  return { employees, loading, error, refetch: fetchEmployees, createEmployee, updateEmployee, deleteEmployee }
}
