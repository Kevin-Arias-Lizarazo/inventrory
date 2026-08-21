import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'
import { EventosProveedor } from './eventos'
import { AuthProveedor } from './auth/AuthContext'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <AuthProveedor>
      <EventosProveedor>
        <App />
      </EventosProveedor>
    </AuthProveedor>
  </StrictMode>,
)