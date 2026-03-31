import { useCallback, useEffect, useState } from 'react'
import api from '../api/axios'

export function useAttendance() {
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchRecords = useCallback(async (employeeId) => {
    setLoading(true)
    setError('')
    try {
      const url = employeeId ? `/api/attendance/employee/${employeeId}` : '/api/attendance'
      const res = await api.get(url)
      setRecords(res.data?.data ?? res.data ?? [])
    } catch {
      setError('Không thể tải dữ liệu chấm công.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchRecords('') }, [fetchRecords])

  const checkIn = useCallback(async (employeeId) => {
    const res = await api.post('/api/attendance/checkin', { employeeId })
    return res.data?.data ?? res.data
  }, [])

  const checkOut = useCallback(async (attendanceId) => {
    const res = await api.post('/api/attendance/checkout', { attendanceId })
    return res.data?.data ?? res.data
  }, [])

  const logAttendance = useCallback(async (data) => {
    const res = await api.post('/api/attendance', data)
    return res.data?.data ?? res.data
  }, [])

  return { records, loading, error, refetch: fetchRecords, checkIn, checkOut, logAttendance }
}
