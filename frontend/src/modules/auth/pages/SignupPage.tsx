import { Link } from 'react-router-dom'
import { AuthShell } from '../ui/AuthShell'
import { SignupForm } from '../ui/SignupForm'

export function SignupPage() {
  return (
    <AuthShell
      title="Create an account"
      description="Join Dissension today"
      footer={
        <p className="text-center text-(--text-secondary)">
          Already have an account?{' '}
          <Link
            to="/login"
            className="font-semibold text-(--text-display) transition-colors hover:text-[#f6dd53] hover:underline"
          >
            Sign in
          </Link>
        </p>
      }
    >
      <SignupForm />
    </AuthShell>
  )
}
