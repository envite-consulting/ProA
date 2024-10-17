// Composables
import { createRouter, createWebHistory } from 'vue-router'
import { useAppStore } from "@/store/app";

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/default/Default.vue'),
    children: [
      {
        path: '',
        name: 'ProjectOverview',
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        component: () => import('@/views/Home.vue'),
      },
      {
        path: 'CamundaCloudImport',
        name: 'CamundaCloudImport',
        component: () => import('@/views/CamundaCloudImportView.vue'),
      },
      {
        path: '/ProcessView/:id',
        name: 'ProcessView',
        component: () => import('@/views/ProcessView.vue'),
      },
      {
        path: 'ProcessList',
        name: 'ProcessList',
        component: () => import('@/views/ProcessListView.vue'),
      },
      {
        path: 'ProcessMap',
        name: 'ProcessMap',
        component: () => import('@/views/ProcessMapView.vue'),
      },
      {
        path: 'SignIn',
        name: 'SignIn',
        component: () => import('@/views/SignInView.vue'),
      },
      {
        path: '/:pathMatch(.*)*',
        name: 'PageNotFound',
        component: () => import('@/views/PageNotFoundView.vue'),
      }
    ]
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

router.beforeEach((to, from, next) => {
  const store = useAppStore();
  const isLoggedIn = store.getUser() != null;
  const isWebVersion = import.meta.env.VITE_APP_MODE === "web";

  if (to.name === 'SignIn') {
    if (!isWebVersion) {
      window.history.back();
      return;
    }
    next();
    return;
  }

  if (isWebVersion && !isLoggedIn) {
    next({ name: 'SignIn' });
  } else {
    next();
  }
});

export default router
