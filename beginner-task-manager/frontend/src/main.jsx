import React, {useEffect, useState} from 'react';
import {createRoot} from 'react-dom/client';
import {taskApi} from './api';
import './style.css';

const blank = {title: '', description: '', completed: false};

function App() {
  const [tasks, setTasks] = useState([]);
  const [form, setForm] = useState({...blank});
  const [editing, setEditing] = useState(null);
  const [filter, setFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [confirmDelete, setConfirmDelete] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    let stopped = false;
    taskApi('', {signal: controller.signal})
      .then(data => {if (!stopped) setTasks(data);})
      .catch(error => {if (!stopped && error.name !== 'AbortError') setError(error.message);})
      .finally(() => {if (!stopped) setLoading(false);});
    return () => {stopped = true; controller.abort();};
  }, []);

  function resetForm() {
    setEditing(null);
    setForm({...blank});
  }

  async function save(event) {
    event.preventDefault();
    setBusy(true); setError(''); setMessage('');
    try {
      const saved = await taskApi(editing ? '/' + editing : '', {
        method: editing ? 'PUT' : 'POST', body: JSON.stringify(form)
      });
      setTasks(current => editing ? current.map(task => task.id === saved.id ? saved : task) : [saved, ...current]);
      setMessage(editing ? 'Task updated.' : 'Task added. One step forward.');
      resetForm();
    } catch (error) {setError(error.message);}
    finally {setBusy(false);}
  }

  async function toggle(task) {
    setBusy(true); setError(''); setMessage('');
    try {
      const saved = await taskApi('/' + task.id, {
        method: 'PUT', body: JSON.stringify({title: task.title, description: task.description, completed: !task.completed})
      });
      setTasks(current => current.map(item => item.id === saved.id ? saved : item));
      if (editing === saved.id) setForm(current => ({...current, completed: saved.completed}));
      setMessage(saved.completed ? 'Task completed.' : 'Task reopened.');
    } catch (error) {setError(error.message);}
    finally {setBusy(false);}
  }

  async function remove(task) {
    setBusy(true); setError(''); setMessage('');
    try {
      await taskApi('/' + task.id, {method: 'DELETE'});
      setTasks(current => current.filter(item => item.id !== task.id));
      if (editing === task.id) resetForm();
      setConfirmDelete(null); setMessage('Task deleted.');
    } catch (error) {setError(error.message);}
    finally {setBusy(false);}
  }

  async function reload() {
    setLoading(true); setError('');
    try {setTasks(await taskApi());}
    catch (error) {setError(error.message);}
    finally {setLoading(false);}
  }

  const done = tasks.filter(task => task.completed).length;
  const shown = tasks.filter(task => filter === 'all' || (filter === 'done' ? task.completed : !task.completed));

  return <>
    <header className="topbar"><a className="brand" href="#main"><span className="brand-mark">✓</span> Taskboard<span className="version">01</span></a><span className="owner">Naga Sai Narra / Java + React</span></header>
    <main id="main">
      <section className="intro"><p className="eyebrow">SMALL TASKS. STEADY PROGRESS.</p><h1>Make room for<br/><span>what’s next.</span></h1><p>A simple place to plan, finish, and keep moving.</p></section>
      <div className="stats" aria-label="Task counts"><div><strong>{tasks.length}</strong><span>Total tasks</span></div><div><strong>{tasks.length - done}</strong><span>In progress</span></div><div><strong>{done}</strong><span>Completed</span></div></div>
      {error && <div className="error" role="alert">{error} <button onClick={reload} disabled={loading || busy}>Try again</button></div>}
      <p className="status" role="status">{message}</p>
      <div className="workspace">
        <section className="panel form-panel" aria-labelledby="form-title"><p className="eyebrow">01 / PLAN IT</p><h2 id="form-title">{editing ? 'Edit your task' : 'Add something new'}</h2>
          <form onSubmit={save}><fieldset disabled={busy || loading}>
            <label htmlFor="title">Task title</label><input id="title" required maxLength={100} value={form.title} onChange={event => setForm({...form, title: event.target.value})} placeholder="e.g. Build my first REST API"/>
            <label htmlFor="description">Notes <span>(optional)</span></label><textarea id="description" rows={4} maxLength={1000} value={form.description} onChange={event => setForm({...form, description: event.target.value})} placeholder="Give yourself a useful starting point…"/>
            <label className="check-label"><input type="checkbox" checked={form.completed} onChange={event => setForm({...form, completed: event.target.checked})}/> Already completed</label>
            <button className="primary" type="submit">{busy ? 'Saving…' : editing ? 'Save changes ↗' : 'Add task ↗'}</button>
            {editing && <button className="secondary" type="button" onClick={resetForm}>Cancel editing</button>}
          </fieldset></form><p className="form-note">Start small. You can always edit the details.</p>
        </section>
        <section className="panel list-panel" aria-labelledby="list-title"><div className="list-heading"><div><p className="eyebrow">02 / DO IT</p><h2 id="list-title">Your task list</h2></div><button className="refresh" disabled={busy || loading} onClick={reload} aria-label="Refresh tasks">↻</button></div>
          <div className="filters" aria-label="Filter tasks">{[['all', 'All'], ['active', 'In progress'], ['done', 'Completed']].map(([value, label]) => <button key={value} aria-pressed={filter === value} onClick={() => setFilter(value)}>{label}</button>)}</div>
          {loading ? <p className="empty" role="status">Loading your tasks…</p> : shown.length === 0 ? <div className="empty"><span className="empty-icon">↗</span><h3>{tasks.length ? 'Nothing in this view.' : 'A fresh start.'}</h3><p>{tasks.length ? 'Try another filter to see your tasks.' : 'Add your first task using the form.'}</p></div> : <ul className="task-list">{shown.map(task => <li key={task.id} className={task.completed ? 'task completed' : 'task'}>
            <button className="task-check" disabled={busy || loading} onClick={() => toggle(task)} aria-label={(task.completed ? 'Reopen ' : 'Complete ') + task.title} aria-pressed={task.completed}>{task.completed ? '✓' : ''}</button>
            <div className="task-content"><h3>{task.title}</h3>{task.description && <p>{task.description}</p>}<span className="task-date">Added {new Date(task.createdAt).toLocaleDateString(undefined, {month: 'short', day: 'numeric'})}</span>
              <div className="task-actions"><button disabled={busy || loading} onClick={() => {setEditing(task.id); setForm({title: task.title, description: task.description, completed: task.completed}); setError(''); setMessage('Editing: ' + task.title); document.querySelector('#title').focus();}}>Edit</button><button className="danger-link" disabled={busy || loading} onClick={() => setConfirmDelete(task.id)}>Delete</button></div>
              {confirmDelete === task.id && <div className="delete-confirm" role="group" aria-label={'Delete ' + task.title + '?'}><span>Delete this task?</span><button disabled={busy} onClick={() => remove(task)}>Yes, delete</button><button disabled={busy} onClick={() => setConfirmDelete(null)}>Keep it</button></div>}
            </div>
          </li>)}</ul>}
        </section>
      </div>
    </main>
    <footer>Designed &amp; built by Naga Sai Narra <span>One task at a time.</span></footer>
  </>;
}

createRoot(document.getElementById('root')).render(<App/>);
