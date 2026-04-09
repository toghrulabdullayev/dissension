import { cva, type VariantProps } from 'class-variance-authority'
import type { ButtonHTMLAttributes } from 'react'
import { cn } from '../lib/cn'

const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 rounded-full border text-[13px] font-normal uppercase tracking-[0.08em] transition-colors duration-200 ease-out focus-visible:outline-none disabled:pointer-events-none disabled:opacity-50 nd-label',
  {
    variants: {
      variant: {
        default:
          'border-(--text-display) bg-(--text-display) text-(--black) hover:bg-(--text-primary) hover:border-(--text-primary)',
        outline:
          'border-(--border-visible) bg-transparent text-(--text-primary) hover:border-(--text-primary) hover:text-(--text-display)',
        ghost:
          'border-transparent bg-transparent text-(--text-secondary) hover:border-(--border) hover:text-(--text-display)',
      },
      size: {
        default: 'h-11 px-5',
        sm: 'h-9 px-3.5',
        lg: 'h-12 px-7',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
)

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> &
  VariantProps<typeof buttonVariants>

export function Button({ className, variant, size, ...props }: ButtonProps) {
  return (
    <button
      className={cn(buttonVariants({ variant, size }), className)}
      {...props}
    />
  )
}
