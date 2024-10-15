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

  if (to.name === 'SignIn') {
    next();
    return;
  }

  if (!isLoggedIn && to.name && routes[0].children.map(r => r.name).includes(to.name.toString())) {
    next({ name: 'SignIn' });
  } else {
    next();
  }
});

export default router
