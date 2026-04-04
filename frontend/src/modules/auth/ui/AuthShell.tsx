import { ShieldCheck } from 'lucide-react'
import type { ReactNode } from 'react'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../../../shared/ui/card'

type AuthShellProps = {
  title: string
  description: string
  children: ReactNode
  footer: ReactNode
}

export function AuthShell({ title, description, children, footer }: AuthShellProps) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-10">
      <div className="mx-auto w-full max-w-md">
        <Card className="border-0">
          <CardHeader className="text-center">
            <div className="mx-auto mb-2 flex h-10 w-10 items-center justify-center rounded-full bg-slate-900 text-white">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <CardTitle>{title}</CardTitle>
            <CardDescription>{description}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-5">{children}</CardContent>
          <div className="border-t border-slate-200 px-6 py-5 text-sm text-slate-600">{footer}</div>
        </Card>
      </div>
    </div>
  )
}
