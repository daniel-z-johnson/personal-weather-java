import { useCallback, useEffect, useState } from 'react'
import { errorText, getWeather, type LocationWeather } from './api'
import { LocationCard } from './components/LocationCard'
import { SearchPanel } from './components/SearchPanel'

function App() {
  /** Null until the first load finishes. */
  const [locations, setLocations] = useState<LocationWeather[] | null>(null)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [refreshing, setRefreshing] = useState(false)

  const load = useCallback(
    (signal?: AbortSignal) =>
      getWeather(signal).then(
        (data) => {
          setLocations(data)
          setLoadError(null)
        },
        (e) => {
          if (signal?.aborted) return
          setLoadError(errorText(e))
        },
      ),
    [],
  )

  useEffect(() => {
    const controller = new AbortController()
    load(controller.signal)
    return () => controller.abort()
  }, [load])

  async function refresh() {
    setRefreshing(true)
    await load()
    setRefreshing(false)
  }

  function removed(id: number) {
    setLocations((current) => current?.filter((l) => l.id !== id) ?? null)
  }

  return (
    <div className="min-h-svh bg-slate-50 text-slate-700 dark:bg-slate-900 dark:text-slate-300">
      <main className="mx-auto max-w-5xl px-4 py-10 sm:px-6">
        <header className="flex items-center justify-between gap-4">
          <h1 className="text-3xl font-bold tracking-tight text-slate-900 dark:text-slate-50">Personal Weather</h1>
          <button
            type="button"
            onClick={refresh}
            disabled={refreshing || locations === null}
            className="rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-sm font-medium text-slate-700 hover:bg-slate-100 focus-visible:outline-2 focus-visible:outline-sky-500 disabled:opacity-50 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-200 dark:hover:bg-slate-700"
          >
            {refreshing ? 'Refreshing…' : 'Refresh'}
          </button>
        </header>

        <div className="mt-8">
          <SearchPanel onSaved={() => load()} />
        </div>

        <section aria-labelledby="saved-heading" className="mt-10">
          <h2 id="saved-heading" className="text-lg font-semibold text-slate-900 dark:text-slate-50">
            Saved locations
          </h2>

          {loadError && (
            <p role="alert" className="mt-4 rounded-lg bg-red-50 px-4 py-3 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
              {loadError}
            </p>
          )}

          {locations === null && !loadError && <p className="mt-4 text-sm text-slate-500">Loading…</p>}

          {locations?.length === 0 && (
            <p className="mt-4 text-sm text-slate-500 dark:text-slate-400">
              No saved locations yet. Search for a city above to add one.
            </p>
          )}

          {locations && locations.length > 0 && (
            <ul className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {locations.map((location) => (
                <LocationCard key={location.id} location={location} onRemoved={removed} />
              ))}
            </ul>
          )}
        </section>
      </main>
    </div>
  )
}

export default App
