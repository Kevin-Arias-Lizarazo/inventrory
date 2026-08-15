import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { EventosProveedor } from './eventos'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <EventosProveedor>
      <App />
    </EventosProveedor>
  </StrictMode>,
)