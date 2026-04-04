import { Link } from 'react-router-dom'
import { AuthShell } from '../ui/AuthShell'
import { LoginForm } from '../ui/LoginForm'

export function LoginPage() {
  return (
    <AuthShell
      title="Welcome to Dissension"
      description="Sign in to your account"
      footer={
        <p className="text-center">
          Do not have an account?{' '}
          <Link to="/signup" className="font-semibold text-slate-900 hover:underline">
            Sign up
          </Link>
        </p>
      }
    >
      <LoginForm />
    </AuthShell>
  )
}
