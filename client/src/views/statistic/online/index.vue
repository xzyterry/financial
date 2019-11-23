<template>
  <div>
    <el-row>
      <el-card class="box-card">
        <div style="display:inline;">
          <el-date-picker
            v-model="dateStr"
            type="date"
            placeholder="选择日期"
            value-format="yyyy-MM-dd"
            @change="handleChange"
          />
          <el-select v-model="roomName" placeholder="选择房间" @change="handleRoomChange">
            <el-option
              v-for="item in filter"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </div>
        <div id="main" style="height:400px; margin-top:10px" />
      </el-card>
    </el-row>

  </div>
</template>

<script>

import { fetchList as fetchOnlineList } from '@/api/statistic.js'
import { formatDate } from '@/utils/date.js'
export default {
  data() {
    return {
      filter:	[
        '全部房间', '经典初级场', '经典中级场', '经典高级场', '经典大师场', '经典加倍场',
        '经典双人场', '排位单排场', '好友经典场', '双人好友场', '比赛复活房', '比赛连胜场',
        '不洗牌', '总数'
      ],
      category:	[
        '经典初级场', '经典中级场', '经典高级场', '经典大师场',
        '经典加倍场', '经典双人场', '排位单排场', '好友经典场',
        '双人好友场', '比赛复活房', '比赛连胜场', '不洗牌', '总数'
      ],
      xs: [],
      ys: [],
      Ys: [],
      dateStr: '',
      roomName: '',
      onlineDatas: []
    }
  },
  mounted() {
    this.dateStr = formatDate(new Date(), 'yyyy-MM-dd')
    this.fetchList(this.dateStr)
    this.roomName = this.filter[0]
  },
  methods: {
    handleRoomChange() {
      this.ys = []
      var idx = this.category.indexOf(this.roomName)
      if (idx === -1) {
        this.fetchList(this.dateStr)
        return
      }
      this.ys = this.Ys[idx]
      this.init()
    },
    handleChange() {
      this.fetchList(this.dateStr)
    },
    fetchList(dateStr) {
      fetchOnlineList({ queryDate: dateStr }).then(resolve => {
        this.onlineDatas = resolve.data.onlineDatas
        var onlineDatas = this.onlineDatas

        var xs = []
        var Ys = []

        for (var k = 0; k < onlineDatas.length; k++) {
          var ms = new Date(onlineDatas[k][0])
          xs.push(formatDate(ms, 'hh:mm'))
        }

        for (var i = 1; i < this.category.length; i++) {
          var obj = {}
          obj.name = this.category[i - 1]
          obj.type = 'line'
          obj.data = []
          for (var j = 0; j < onlineDatas.length; j++) {
            obj.data.push(onlineDatas[j][i])
          }
          Ys.push(obj)
        }
        this.xs = xs
        this.Ys = Ys
        this.ys = Ys
        this.init()
      }).catch(err => {
        console.log(err)
      })
    },
    init() {
      var myChart = this.$echarts.init(document.getElementById('main'))

      // 指定图表的配置项和数据
      var option = {
        title: {
          text: '实时在线人数',
          subtext: ''
        },
        tooltip: {
          trigger: 'axis',
          confine: true
        },
        legend: {
          orient: 'vertical',
          x: 'right',
          y: 'middle',
          data: this.category
        },
        toolbox: {
          show: true,
          feature: {
            dataZoom: {
              yAxisIndex: 'none'
            },
            dataView: { readOnly: false },
            magicType: { type: ['line', 'bar'] },
            restore: {},
            saveAsImage: {}
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: this.xs
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value} '
          }
        },
        series: this.ys
      }

      // 使用刚指定的配置项和数据显示图表。
      myChart.setOption(option, true)
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
