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
    <div
      className="flex min-h-screen items-center justify-center bg-(--black) px-4 py-10"
      style={{
        backgroundImage:
          'radial-gradient(circle at 10% 12%, rgba(192,72,63,0.24), transparent 35%), radial-gradient(circle at 90% 88%, rgba(246,221,83,0.18), transparent 36%)',
      }}
    >
      <div className="mx-auto w-full max-w-md">
        <Card className="border-(--border-visible) bg-(--surface)">
          <CardHeader className="text-center">
            <div className="mx-auto mb-2 h-14 w-14 overflow-hidden rounded-2xl border border-(--border-visible) bg-(--surface-raised) p-2">
              <img src="/logo.png" alt="Dissension logo" className="h-full w-full object-contain" />
            </div>
            <CardTitle className="bg-linear-to-r from-[#c0483f] via-[#eda642] to-[#f6dd53] bg-clip-text text-transparent">
              {title}
            </CardTitle>
            <CardDescription>{description}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-5">{children}</CardContent>
          <div className="border-t border-(--border) px-6 py-5 text-sm text-(--text-secondary)">{footer}</div>
        </Card>
      </div>
    </div>
  )
}
