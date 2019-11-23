<template>
  <div>
    <el-row>
      <el-card class="box-card">
        <div style="display:inline">
          <el-date-picker
            v-model="dateStr"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            @change="handleChange"
          />
        </div>
        <div id="main" style="height:400px;" />
      </el-card>
    </el-row>

  </div>
</template>

<script>

import { formatDate } from '@/utils/date.js'
import { fetchGoldEggList } from '@/api/statistic.js'
export default {
  data() {
    return {
      dict: {
        '1001': '经典初级场', '1002': '经典中级场', '1003': '经典高级场', '1004': '经典大师场',
        '1005': '经典加倍场', '1006': '经典双人场', '2001': '排位单排场', '3001': '好友经典场',
        '3002': '双人好友场', '4000': '比赛复活房', '4001': '比赛连胜场', '5001': '不洗牌' },
      dateStr: '',
      ys: [],
      left: [],
      right: []
    }
  },
  mounted() {
    this.dateStr = formatDate(new Date(), 'yyyy-MM-dd')
    this.fetchList(this.dateStr)
  },
  methods: {
    handleChange() {
      this.fetchList(this.dateStr)
    },
    clean() {
      this.ys = []
      this.left = []
      this.right = []
    },
    fetchList(dateStr) {
      this.clean()
      fetchGoldEggList({ changeDate: dateStr }).then(resolve => {
        var obj = resolve.data
        if (obj === null) {
          return
        }
        var obj2 = obj.roomJinDanChangeStatisticsData
        if (obj2 === null) {
          return
        }
        var data = obj2.changeData
        for (var i = 0; i < data.length; i++) {
          var roomId = data[i]['changeRoomId']
          var changeCount = data[i]['changeCount']
          var isAdd = data[i]['add']
          this.ys.push(this.dict[roomId])
          if (isAdd) {
            this.right.push(changeCount)
          } else {
            this.left.push(-changeCount)
          }
        }
        this.init()
      })
    },
    init() {
      var myChart = this.$echarts.init(document.getElementById('main'))

      // 指定图表的配置项和数据
      var option = {
        title: {
          text: '金蛋增加消耗数',
          left: '40%',
          top: '10px'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { // 坐标轴指示器，坐标轴触发有效
            type: 'shadow' // 默认为直线，可选为：'line' | 'shadow'
          }
        },
        legend: {
          data: ['消耗', '增加'],
          orient: 'vertical',
          x: 'right',
          y: 'middle'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: [
          {
            type: 'value'
          }
        ],
        yAxis: [
          {
            type: 'category',
            axisTick: { show: false },
            data: this.ys
          }
        ],
        series: [

          {
            name: '消耗',
            type: 'bar',
            stack: '总量',
            label: {
              normal: {
                show: true
              }
            },
            data: this.left
          },
          {
            name: '增加',
            type: 'bar',
            stack: '总量',
            label: {
              normal: {
                show: true
              }
            },
            data: this.right
          }
        ]
      }

      // 使用刚指定的配置项和数据显示图表。
      myChart.setOption(option)
    }
  }
}
</script>

<style lang="scss">
.box-card{
  width: 90%
}

.el-row{
  margin: 10px 20px;
}

.el-input__inner{
  border:none
}
</style>
