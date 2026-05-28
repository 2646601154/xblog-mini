<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import type { EChartsOption } from 'echarts'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  PieChart,
  BarChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
])

// Props
const props = defineProps<{
  articleTrend?: {
    dates: string[]
    counts: number[]
  }
  categoryDistribution?: {
    name: string
    value: number
  }[]
  tagUsage?: {
    tags: string[]
    counts: number[]
  }
}>()

// 文章趋势图配置
const articleTrendOption = computed<EChartsOption>(() => ({
  title: {
    text: '文章发布趋势',
    left: 'center',
  },
  tooltip: {
    trigger: 'axis',
  },
  xAxis: {
    type: 'category',
    data: props.articleTrend?.dates || [],
  },
  yAxis: {
    type: 'value',
  },
  series: [
    {
      name: '文章数',
      type: 'line',
      data: props.articleTrend?.counts || [],
      smooth: true,
      itemStyle: {
        color: '#409EFF',
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' },
          ],
        },
      },
    },
  ],
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
}))

// 分类分布图配置
const categoryOption = computed<EChartsOption>(() => ({
  title: {
    text: '分类分布',
    left: 'center',
  },
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)',
  },
  legend: {
    orient: 'vertical',
    left: 'left',
  },
  series: [
    {
      name: '文章数',
      type: 'pie',
      radius: '60%',
      data: props.categoryDistribution || [],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)',
        },
      },
    },
  ],
}))

// 标签使用图配置
const tagUsageOption = computed<EChartsOption>(() => ({
  title: {
    text: '标签使用统计',
    left: 'center',
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow',
    },
  },
  xAxis: {
    type: 'category',
    data: props.tagUsage?.tags || [],
    axisLabel: {
      rotate: 30,
    },
  },
  yAxis: {
    type: 'value',
  },
  series: [
    {
      name: '使用次数',
      type: 'bar',
      data: props.tagUsage?.counts || [],
      itemStyle: {
        color: '#67C23A',
      },
    },
  ],
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
}))
</script>

<template>
  <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
    <!-- 文章趋势图 -->
    <el-card shadow="hover" class="col-span-1 lg:col-span-2">
      <v-chart class="h-80" :option="articleTrendOption" autoresize />
    </el-card>

    <!-- 分类分布图 -->
    <el-card shadow="hover" class="col-span-1">
      <v-chart class="h-80" :option="categoryOption" autoresize />
    </el-card>

    <!-- 标签使用图 -->
    <el-card shadow="hover" class="col-span-1">
      <v-chart class="h-80" :option="tagUsageOption" autoresize />
    </el-card>
  </div>
</template>

<style scoped>
.h-80 {
  height: 320px;
}
</style>
