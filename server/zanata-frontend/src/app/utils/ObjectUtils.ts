// Should be pretty fast: https://stackoverflow.com/a/34491287/14379
export function isEmptyObject (obj: any) {
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      return false
    }
  }
  return true
}
