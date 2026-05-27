import { useEffect, useState } from 'react'
import { MdAdd, MdEdit, MdDelete, MdLocationOn } from 'react-icons/md'
import api from '../api/axios'
import Modal from '../components/Modal'
import OfficeMap from '../components/OfficeMap'
import { useTranslation } from '../context/LanguageContext'
import { EmptyState } from '../components/Illustrations'

const emptyForm = {
  name: '',
  address: '',
  latitude: '',
  longitude: '',
  radiusMeters: 100,
  status: 'ACTIVE',
}

export default function OfficeLocations() {
  const { t } = useTranslation()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)

  const load = async () => {
    try {
      setLoading(true)
      const res = await api.get('/api/office-locations')
      setItems(res.data?.data || [])
      setError('')
    } catch (err) {
      setError(err.response?.data?.message || t('Không tải được danh sách văn phòng.'))
    } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  const openCreate = () => {
    setEditingId(null); setForm(emptyForm); setModalOpen(true)
  }
  const openEdit = (o) => {
    setEditingId(o.id)
    setForm({
      name: o.name || '',
      address: o.address || '',
      latitude: String(o.latitude ?? ''),
      longitude: String(o.longitude ?? ''),
      radiusMeters: o.radiusMeters || 100,
      status: o.status || 'ACTIVE',
    })
    setModalOpen(true)
  }

  const useMyLocation = () => {
    if (!('geolocation' in navigator)) return
    navigator.geolocation.getCurrentPosition(
      (p) => setForm((f) => ({
        ...f,
        latitude: p.coords.latitude.toFixed(6),
        longitude: p.coords.longitude.toFixed(6),
      })),
      () => {},
      { enableHighAccuracy: true, timeout: 8000 },
    )
  }

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      const body = {
        name: form.name.trim(),
        address: form.address.trim() || null,
        latitude: Number(form.latitude),
        longitude: Number(form.longitude),
        radiusMeters: Number(form.radiusMeters),
        status: form.status,
      }
      if (editingId) {
        await api.put(`/api/office-locations/${editingId}`, body)
      } else {
        await api.post('/api/office-locations', body)
      }
      setModalOpen(false)
      await load()
    } catch (err) {
      setError(err.response?.data?.message || t('Lưu thất bại.'))
    } finally { setSaving(false) }
  }

  const handleDelete = async (o) => {
    if (!confirm(t('Xoá văn phòng "{name}"?', { name: o.name }))) return
    try {
      await api.delete(`/api/office-locations/${o.id}`)
      await load()
    } catch (err) {
      setError(err.response?.data?.message || t('Xoá thất bại.'))
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">{t('Văn phòng & Geofence')}</h1>
          <p className="text-sm text-slate-500">{t('Cấu hình điểm cho phép chấm công bằng GPS. Bán kính tính bằng mét.')}</p>
        </div>
        <button
          onClick={openCreate}
          className="flex items-center gap-2 bg-brand-600 hover:bg-brand-700 text-white px-4 py-2 rounded-lg text-sm font-medium shadow-brand-glow"
        >
          <MdAdd className="text-xl" /> {t('Thêm văn phòng')}
        </button>
      </div>

      {error && <div className="p-3 mb-4 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm">{error}</div>}

      <div className="mb-6">
        <OfficeMap offices={items} height={360} />
      </div>

      <div className="bg-white rounded-2xl shadow-soft ring-1 ring-slate-200/70 overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-brand-600" />
          </div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Tên văn phòng')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Địa chỉ')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Toạ độ')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Bán kính')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Trạng thái')}</th>
                <th className="px-6 py-3 text-left text-[11px] font-semibold text-slate-500 uppercase tracking-[0.12em]">{t('Hành động')}</th>
              </tr>
            </thead>
            <tbody>
              {items.length === 0 ? (
                <tr><td colSpan={6} className="py-4"><EmptyState text={t('Chưa có văn phòng nào.')} /></td></tr>
              ) : items.map((o, idx) => (
                <tr key={o.id} className={idx % 2 === 0 ? 'bg-white hover:bg-slate-50' : 'bg-slate-50/60 hover:bg-slate-100'}>
                  <td className="px-6 py-4 font-medium text-slate-900">{o.name}</td>
                  <td className="px-6 py-4 text-slate-600">{o.address || '-'}</td>
                  <td className="px-6 py-4 text-slate-600 font-mono text-xs">{o.latitude}, {o.longitude}</td>
                  <td className="px-6 py-4 text-slate-600">{o.radiusMeters}m</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-0.5 text-[11px] rounded-full font-medium ${
                      o.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-200 text-slate-600'
                    }`}>{o.status}</span>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex gap-1">
                      <button onClick={() => openEdit(o)} className="p-1.5 rounded-md hover:bg-brand-50 text-brand-700"><MdEdit /></button>
                      <button onClick={() => handleDelete(o)} className="p-1.5 rounded-md hover:bg-rose-100 text-rose-700"><MdDelete /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)}
             title={editingId ? t('Sửa văn phòng') : t('Thêm văn phòng')}>
        <form onSubmit={handleSave} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">{t('Tên *')}</label>
            <input
              type="text" required value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">{t('Địa chỉ')}</label>
            <input
              type="text" value={form.address}
              onChange={(e) => setForm({ ...form, address: e.target.value })}
              className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-brand-500"
            />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Latitude *</label>
              <input
                type="number" step="any" required value={form.latitude}
                onChange={(e) => setForm({ ...form, latitude: e.target.value })}
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm font-mono focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Longitude *</label>
              <input
                type="number" step="any" required value={form.longitude}
                onChange={(e) => setForm({ ...form, longitude: e.target.value })}
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm font-mono focus:ring-2 focus:ring-brand-500"
              />
            </div>
          </div>
          <button type="button" onClick={useMyLocation}
                  className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-md bg-brand-50 text-brand-700 hover:bg-brand-100">
            <MdLocationOn /> {t('Dùng vị trí hiện tại')}
          </button>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">{t('Bán kính (m)')}</label>
              <input
                type="number" min="10" max="5000" value={form.radiusMeters}
                onChange={(e) => setForm({ ...form, radiusMeters: e.target.value })}
                className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-brand-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">{t('Trạng thái')}</label>
              <select value={form.status}
                      onChange={(e) => setForm({ ...form, status: e.target.value })}
                      className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-brand-500">
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)}
                    className="flex-1 px-4 py-2 border border-slate-200 text-slate-700 rounded-lg text-sm hover:bg-slate-50">{t('Hủy')}</button>
            <button type="submit" disabled={saving}
                    className="flex-1 bg-brand-600 hover:bg-brand-700 disabled:bg-brand-300 text-white px-4 py-2 rounded-lg text-sm font-medium shadow-brand-glow disabled:shadow-none">
              {saving ? t('Đang lưu...') : t('Lưu')}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
