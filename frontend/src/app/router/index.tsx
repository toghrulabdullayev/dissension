import { createRouter, createRootRoute, createRoute, Outlet } from '@tanstack/react-router';

const rootRoute = createRootRoute({
  component: () => <Outlet />,
});

// ---------------------------------------------------------------
// Route stubs — pages will be wired per-phase
// ---------------------------------------------------------------
const loginRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/login',
  component: () => <div>Login</div>,
});

const registerRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/register',
  component: () => <div>Register</div>,
});

const serverRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/server/$serverId',
  component: () => <div>Server</div>,
});

const directMessagesRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/dm/$conversationId',
  component: () => <div>Direct Messages</div>,
});

const callRoomRoute = createRoute({
  getParentRoute: () => rootRoute,
  path: '/call/$channelId',
  component: () => <div>Call Room</div>,
});

const routeTree = rootRoute.addChildren([
  loginRoute,
  registerRoute,
  serverRoute,
  directMessagesRoute,
  callRoomRoute,
]);

export const router = createRouter({ routeTree });

declare module '@tanstack/react-router' {
  interface Register {
    router: typeof router;
  }
}
