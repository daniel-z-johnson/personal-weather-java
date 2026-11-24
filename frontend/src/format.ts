/** "Minnesota, US", or just "MC" for a place with no state. */
export function placeLine(place: { state: string | null; country: string }): string {
  return [place.state, place.country].filter(Boolean).join(', ')
}
