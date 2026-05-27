import { useCallback, useEffect, useState } from 'react'
import api from '../api/axios'

export function useProjects() {
  const [projects, setProjects] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const fetchProjects = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const res = await api.get('/api/projects')
      setProjects(res.data?.data ?? res.data ?? [])
    } catch {
      setError('Không thể tải danh sách dự án.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchProjects() }, [fetchProjects])

  const createProject = useCallback(async (data) => {
    const res = await api.post('/api/projects', data)
    await fetchProjects()
    return res.data?.data ?? res.data
  }, [fetchProjects])

  const updateProject = useCallback(async (id, data) => {
    const res = await api.put(`/api/projects/${id}`, data)
    await fetchProjects()
    return res.data?.data ?? res.data
  }, [fetchProjects])

  const deleteProject = useCallback(async (id) => {
    await api.delete(`/api/projects/${id}`)
    await fetchProjects()
  }, [fetchProjects])

  return { projects, loading, error, refetch: fetchProjects, createProject, updateProject, deleteProject }
}
