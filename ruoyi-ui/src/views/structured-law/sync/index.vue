<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="sync"
          v-hasPermi="['structured-law:provision:sync']"
        >开始解析同步</el-button>
      </el-col>
    </el-row>
    <div class="resp_area" v-html="progressDetail"></div>
  </div>
</template>

<script>
  import { sync, syncProgress } from "@/api/structured-law/provision";
  import Util from '@/utils/util';

  export default {
    name: "Sync",
    components: {

    },
    data() {
      return {
        intervalId: null,
        progressDetail: "",
        isProgressFetching: false
      };
    },
    created() {

    },
    deactivated() {
      if(this.intervalId != null) {
        clearInterval(this.intervalId);
      }
    },
    methods: {
      /** 查询法律条款列表 */
      getList() {
        if(this.intervalId != null) {
          clearInterval(this.intervalId);
        }

        this.intervalId = setInterval(() => {
          this.progress();
        }, 3000)
      },
      sync() {
        sync().then(() => {
          this.$alert('同步过程正在异步进行...', '提示', {
            confirmButtonText: '确定',
            callback: action => {}
          });
        });
        this.getList();
      },
      progress() {
        if(this.isProgressFetching) {
          return
        }

        this.isProgressFetching = true;
        syncProgress(this.queryParams).then(response => {
          this.progressDetail = response.msg;
          this.isProgressFetching = false;
        });
      }
    }
  };
</script>

<style>
  .resp_area {
    border: solid 1px #eee;
    height: 80vh;
    border-radius: 8px;
  }
</style>
