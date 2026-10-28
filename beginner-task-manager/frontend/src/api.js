export async function taskApi(path = '', options = {}) {
  let response;
  try {
    response = await fetch('/api/tasks' + path, {
      ...options,
      headers: {'Content-Type': 'application/json', ...options.headers}
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new Error('Could not connect. Check that the Java backend is running.');
  }
  if (response.status === 204) return null;
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new Error(body?.detail || `Request failed (${response.status}).`);
  if (body === null) throw new Error('The API returned an unexpected response.');
  return body;
}
