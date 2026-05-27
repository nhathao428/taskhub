import { useCallback, useEffect, useState } from 'react'
import api from '../api/axios'

export function useTasks() {
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchTasks = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/tasks')
      setTasks(res.data?.data ?? res.data ?? [])
    } catch {
      setError('Không thể tải danh sách công việc.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchTasks() }, [fetchTasks])

  const createTask = useCallback(async (data) => {
    const res = await api.post('/api/tasks', data)
    await fetchTasks()
    return res.data?.data ?? res.data
  }, [fetchTasks])

  const updateTask = useCallback(async (id, data) => {
    const res = await api.put(`/api/tasks/${id}`, data)
    await fetchTasks()
    return res.data?.data ?? res.data
  }, [fetchTasks])

  const deleteTask = useCallback(async (id) => {
    await api.delete(`/api/tasks/${id}`)
    await fetchTasks()
  }, [fetchTasks])

  return { tasks, loading, error, refetch: fetchTasks, createTask, updateTask, deleteTask }
}
