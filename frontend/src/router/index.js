
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('../layouts/StudentLayout.vue'),
    children: [
      { path: 'dashboard', name: 'StudentDashboard', component: () => import('../views/student/Dashboard.vue') },
      { path: 'repair', name: 'StudentRepair', component: () => import('../views/student/RepairForm.vue') },
      { path: 'history', name: 'StudentHistory', component: () => import('../views/student/RepairHistory.vue') },
      { path: 'detail/:id', name: 'StudentDetail', component: () => import('../views/student/OrderDetail.vue') }
    ]
  },
  {
    path: '/repairman',
    name: 'RepairmanLayout',
    component: () => import('../layouts/RepairmanLayout.vue'),
    children: [
      { path: 'pending', name: 'RepairmanPending', component: () => import('../views/repairman/PendingOrders.vue') },
      { path: 'history', name: 'RepairmanHistory', component: () => import('../views/repairman/OrderHistory.vue') },
      { path: 'detail/:id', name: 'RepairmanDetail', component: () => import('../views/repairman/OrderDetail.vue') }
    ]
  },
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('../layouts/AdminLayout.vue'),
    redirect: '/admin/statistics',
    children: [
      { path: '', name: 'AdminStatistics', component: () => import('../views/admin/Statistics.vue') },
      { path: 'students', name: 'AdminStudents', component: () => import('../views/admin/StudentManagement.vue') },
      { path: 'repairmen', name: 'AdminRepairmen', component: () => import('../views/admin/RepairmanManagement.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/OrderManagement.vue') },
      { path: 'statistics', name: 'AdminStatistics2', component: () => import('../views/admin/Statistics.vue') },
      { path: 'fault-types', name: 'AdminFaultTypes', component: () => import('../views/admin/FaultTypeManagement.vue') },
      { path: 'buildings', name: 'AdminBuildings', component: () => import('../views/admin/BuildingManagement.vue') },
      { path: 'order-detail/:id', name: 'AdminOrderDetail', component: () => import('../views/admin/OrderDetail.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  if (to.path === '/login') {
    next()
    return
  }
  
  if (!userStore.token) {
    next('/login')
    return
  }
  
  const role = userStore.user?.role
  const path = to.path
  
  if (path.startsWith('/student') && role !== 'STUDENT') {
    next('/login')
    return
  }
  
  if (path.startsWith('/repairman') && role !== 'REPAIR') {
    next('/login')
    return
  }
  
  if (path.startsWith('/admin') && role !== 'ADMIN') {
    next('/login')
    return
  }
  
  // 管理员访问根路径时跳转到统计页面
  if (path === '/admin' || path === '/') {
    next('/admin/statistics')
    return
  }
  
  next()
})

export default router
