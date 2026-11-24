import { useRef, useState, type FormEvent } from 'react'
import { errorText, saveLocation, searchLocations, type SearchResult } from '../api'
import { placeLine } from '../format'

type SaveState = { status: 'saving' } | { status: 'error'; message: string }

const inputClass =
  'w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-slate-900 placeholder:text-slate-400 focus:border-sky-500 focus:outline-2 focus:outline-sky-500/30 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-50'

interface Props {
  /** Called after a result is saved, so the saved list can refresh. */
  onSaved: () => void
}

export function SearchPanel({ onSaved }: Props) {
  const [city, setCity] = useState('')
  const [state, setState] = useState('')
  const [country, setCountry] = useState('')

  const [searching, setSearching] = useState(false)
  const [searchError, setSearchError] = useState<string | null>(null)
  /** Null until the first search, so "no matches" only shows after one. */
  const [results, setResults] = useState<SearchResult[] | null>(null)
  const [saves, setSaves] = useState<Record<string, SaveState>>({})
  /** What the last successful add did, shown once the form has been cleared. */
  const [confirmation, setConfirmation] = useState<string | null>(null)
  const cityInput = useRef<HTMLInputElement>(null)

  async function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!city.trim()) return
    setSearching(true)
    setSearchError(null)
    setConfirmation(null)
    try {
      const found = await searchLocations({ city: city.trim(), state: state.trim(), country: country.trim() })
      setResults(found)
      setSaves({})
    } catch (e) {
      setSearchError(errorText(e))
    } finally {
      setSearching(false)
    }
  }

  async function save(result: SearchResult) {
    setSaves((s) => ({ ...s, [result.id]: { status: 'saving' } }))
    try {
      const { created } = await saveLocation(result.id)
      // Done with this search: clear it so the next one starts fresh.
      setCity('')
      setState('')
      setCountry('')
      setResults(null)
      setSaves({})
      setConfirmation(created ? `Added ${result.city}.` : `${result.city} was already saved.`)
      cityInput.current?.focus()
      onSaved()
    } catch (e) {
      setSaves((s) => ({ ...s, [result.id]: { status: 'error', message: errorText(e) } }))
    }
  }

  return (
    <section
      aria-labelledby="search-heading"
      className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-700 dark:bg-slate-800"
    >
      <h2 id="search-heading" className="text-lg font-semibold text-slate-900 dark:text-slate-50">
        Add a location
      </h2>

      <form onSubmit={search} className="mt-4 grid gap-3 sm:grid-cols-[2fr_1fr_1fr_auto] sm:items-end">
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          City
          <input
            ref={cityInput}
            className={`${inputClass} mt-1`}
            value={city}
            onChange={(e) => setCity(e.target.value)}
            placeholder="Saint Paul"
            maxLength={100}
            required
          />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          State <span className="font-normal text-slate-400">(US only)</span>
          <input
            className={`${inputClass} mt-1`}
            value={state}
            onChange={(e) => setState(e.target.value)}
            placeholder="MN"
            maxLength={100}
          />
        </label>
        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">
          Country
          <input
            className={`${inputClass} mt-1`}
            value={country}
            onChange={(e) => setCountry(e.target.value)}
            placeholder="US"
            maxLength={100}
          />
        </label>
        <button
          type="submit"
          disabled={searching || !city.trim()}
          className="rounded-lg bg-sky-600 px-4 py-2 font-medium text-white hover:bg-sky-700 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-sky-500 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {searching ? 'Searching…' : 'Search'}
        </button>
      </form>

      {searchError && (
        <p role="alert" className="mt-4 text-sm text-red-600 dark:text-red-400">
          {searchError}
        </p>
      )}

      {confirmation && (
        <p role="status" className="mt-4 text-sm font-medium text-emerald-600 dark:text-emerald-400">
          {confirmation}
        </p>
      )}

      {results && results.length === 0 && (
        <p className="mt-4 text-sm text-slate-500 dark:text-slate-400">No places match that search.</p>
      )}

      {results && results.length > 0 && (
        <ul className="mt-4 divide-y divide-slate-200 dark:divide-slate-700">
          {results.map((result) => (
            <SearchResultRow key={result.id} result={result} save={saves[result.id]} onSave={() => save(result)} />
          ))}
        </ul>
      )}
    </section>
  )
}

function SearchResultRow({ result, save, onSave }: { result: SearchResult; save?: SaveState; onSave: () => void }) {
  return (
    <li className="flex items-center justify-between gap-3 py-3">
      <div className="min-w-0">
        <p className="truncate font-medium text-slate-900 dark:text-slate-50">{result.city}</p>
        <p className="truncate text-sm text-slate-500 dark:text-slate-400">
          {placeLine(result)} · {result.latitude.toFixed(2)}, {result.longitude.toFixed(2)}
        </p>
        {save?.status === 'error' && (
          <p role="alert" className="mt-1 text-sm text-red-600 dark:text-red-400">
            {save.message}
          </p>
        )}
      </div>
      <button
        type="button"
        onClick={onSave}
        disabled={save?.status === 'saving'}
        className="shrink-0 rounded-lg border border-sky-600 px-3 py-1.5 text-sm font-medium text-sky-700 hover:bg-sky-50 focus-visible:outline-2 focus-visible:outline-sky-500 disabled:opacity-50 dark:border-sky-400 dark:text-sky-300 dark:hover:bg-sky-950"
      >
        {save?.status === 'saving' ? 'Adding…' : 'Add'}
      </button>
    </li>
  )
}
