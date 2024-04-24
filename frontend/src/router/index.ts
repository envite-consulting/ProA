// Composables
import { createRouter, createWebHistory } from 'vue-router'

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
        component: () => import('@/views/ProjectOverviewView.vue'),
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
    ],
  },
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
})

export default router
