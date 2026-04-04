import { Link } from 'react-router-dom'
import { AuthShell } from '../ui/AuthShell'
import { SignupForm } from '../ui/SignupForm'

export function SignupPage() {
  return (
    <AuthShell
      title="Create an account"
      description="Join Dissension today"
      footer={
        <p className="text-center">
          Already have an account?{' '}
          <Link to="/login" className="font-semibold text-slate-900 hover:underline">
            Sign in
          </Link>
        </p>
      }
    >
      <SignupForm />
    </AuthShell>
  )
}
