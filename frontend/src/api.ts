/** A saved location with its current temperature. The weather fields are null when OpenWeather couldn't provide it. */
export interface LocationWeather {
  id: number
  city: string
  state: string | null
  country: string
  /** Rounded to a tenth. */
  temperatureC: number | null
  /** Rounded to a whole degree. */
  temperatureF: number | null
  /** ISO-8601 instant when OpenWeather took the reading. */
  observedAt: string | null
}

/** A search match. Its [id] can be saved for about an hour before it expires. */
export interface SearchResult {
  id: string
  city: string
  state: string | null
  country: string
  latitude: number
  longitude: number
}

export interface SearchQuery {
  city: string
  state?: string
  country?: string
}

/** A failed API call, with a message fit to show the user. [status] is 0 when the server couldn't be reached. */
export class ApiError extends Error {
  readonly status: number

  constructor(message: string, status: number) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

const UNREACHABLE = "Can't reach the server. Is the backend running?"

async function send(path: string, init?: RequestInit): Promise<Response> {
  let response: Response
  try {
    response = await fetch(path, init)
  } catch (e) {
    if (e instanceof DOMException && e.name === 'AbortError') throw e
    throw new ApiError(UNREACHABLE, 0)
  }
  if (!response.ok) throw new ApiError(await errorMessage(response), response.status)
  return response
}

/** The backend's problem-detail `detail` when there is one, otherwise a generic message. */
async function errorMessage(response: Response): Promise<string> {
  try {
    const body: unknown = await response.json()
    if (body && typeof body === 'object' && 'detail' in body && typeof body.detail === 'string' && body.detail) {
      return body.detail
    }
  } catch {
    // Not JSON: fall through.
  }
  // The backend's own 502s carry a detail, so a bare 502/504 is the dev proxy failing to reach it.
  if (response.status === 502 || response.status === 504) return UNREACHABLE
  return `Something went wrong (${response.status}). Please try again.`
}

export async function getWeather(signal?: AbortSignal): Promise<LocationWeather[]> {
  const response = await send('/api/weather', { signal })
  return response.json()
}

export async function searchLocations(query: SearchQuery, signal?: AbortSignal): Promise<SearchResult[]> {
  const params = new URLSearchParams({ city: query.city })
  if (query.state) params.set('state', query.state)
  if (query.country) params.set('country', query.country)
  const response = await send(`/api/locations/search?${params}`, { signal })
  return response.json()
}

/** Saves a search result. [created] is false when that place was already saved. */
export async function saveLocation(searchResultId: string): Promise<{ created: boolean }> {
  const response = await send('/api/locations', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ id: searchResultId }),
  })
  return { created: response.status === 201 }
}

export async function deleteLocation(id: number): Promise<void> {
  await send(`/api/locations/${id}`, { method: 'DELETE' })
}

export function errorText(e: unknown): string {
  return e instanceof ApiError ? e.message : 'Something went wrong. Please try again.'
}
