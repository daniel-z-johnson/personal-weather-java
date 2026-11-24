import { useState } from 'react'
import { ApiError, deleteLocation, errorText, type LocationWeather } from '../api'
import { placeLine } from '../format'

const timeFormat = new Intl.DateTimeFormat(undefined, { hour: 'numeric', minute: '2-digit' })

interface Props {
  location: LocationWeather
  onRemoved: (id: number) => void
}

export function LocationCard({ location, onRemoved }: Props) {
  const [removing, setRemoving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function remove() {
    setRemoving(true)
    setError(null)
    try {
      await deleteLocation(location.id)
      onRemoved(location.id)
    } catch (e) {
      // Already gone (removed in another tab, say): drop it here too.
      if (e instanceof ApiError && e.status === 404) {
        onRemoved(location.id)
        return
      }
      setError(errorText(e))
      setRemoving(false)
    }
  }

  const hasWeather = location.temperatureF !== null && location.temperatureC !== null

  return (
    <li className="flex flex-col rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-700 dark:bg-slate-800">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <h3 className="truncate text-lg font-semibold text-slate-900 dark:text-slate-50">{location.city}</h3>
          <p className="truncate text-sm text-slate-500 dark:text-slate-400">{placeLine(location)}</p>
        </div>
        <button
          type="button"
          onClick={remove}
          disabled={removing}
          aria-label={`Remove ${location.city}`}
          className="-mr-1 -mt-1 rounded-lg px-2 py-1 text-sm text-slate-400 hover:bg-slate-100 hover:text-red-600 focus-visible:outline-2 focus-visible:outline-sky-500 disabled:opacity-50 dark:hover:bg-slate-700 dark:hover:text-red-400"
        >
          {removing ? 'Removing…' : 'Remove'}
        </button>
      </div>

      {hasWeather ? (
        <div className="mt-4">
          <p className="flex items-baseline gap-3">
            <span className="text-5xl font-light tabular-nums text-slate-900 dark:text-slate-50">
              {location.temperatureF}°F
            </span>
            <span className="text-lg tabular-nums text-slate-500 dark:text-slate-400">
              {location.temperatureC!.toFixed(1)}°C
            </span>
          </p>
          {location.observedAt && (
            <p className="mt-2 text-xs text-slate-500 dark:text-slate-400">
              Observed {timeFormat.format(new Date(location.observedAt))}
            </p>
          )}
        </div>
      ) : (
        <p className="mt-4 text-sm text-slate-500 dark:text-slate-400">
          Weather is unavailable right now. Try refreshing in a moment.
        </p>
      )}

      {error && (
        <p role="alert" className="mt-3 text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      )}
    </li>
  )
}
