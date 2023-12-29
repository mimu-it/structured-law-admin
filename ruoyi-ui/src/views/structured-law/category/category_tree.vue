<template>
  <el-tree :data="treeList" :props="defaultProps" @node-click="nodeClick"></el-tree>
</template>
<script>
  import {listAllCategory} from "@/api/structured-law/category";

  export default {
    name: 'CategoryTree',
    components: {},
    props: {
      visible: {
        type: Boolean,
        default() {
          return false;
        }
      },
      title: {
        type: String,
        default() {
          return "";
        }
      },
      data: {
        type: Object,
        default() {
          return {};
        }
      }
    },
    data() {
      return {
        queryParams: {},
        treeList: [],
        defaultProps: {
          children: 'children',
          label: 'label'
        }
      }
    },
    watch: {},
    created() {
      this.loadCategory()
    },
    mounted() {
      this.$nextTick(() => {
        /* 恢复数据 */

      });
    },
    computed: {},
    methods: {
      nodeClick(data) {
        console.log(data);
        if(data.id == 0) {
          return;
        }

        this.$emit("change", data.id);
      },
      /**
       * 获取楼盘及对应楼栋的数据
       */
      loadCategory() {
        listAllCategory(this.queryParams).then(response => {
          this.treeList = this.makeTree2(response.data);
        });
      },
      makeTree2(list) {
        // 按 categoryOrder 升序排序
        list.sort((a, b) => a.categoryOrder - b.categoryOrder);

        let tree = [];
        list.forEach((item) => {
          let folders = item.folder.split("/");
          if (folders.length == 1) {
            /** 如果folder中不存在"/"，说明根叶子节点 */
            let treeItem = item;
            treeItem.label = item.name;

            this.addRootNodeToTree(tree, treeItem);
            return;
          }

          let virtualItemName = folders[0];
          let rootNode = {};
          rootNode.id = 0;
          rootNode.label = virtualItemName;
          rootNode.folder = virtualItemName;
          rootNode = this.addRootNodeToTree(tree, rootNode);

          this.initChildren(tree, rootNode, folders.length - 1, 1, folders, list);
        });

        return tree;
      },
      initChildren(tree, parentNode, maxLevel, level, folders, list) {
        if(maxLevel === level) {
          //console.log(folders[maxLevel]);
          let pathArr = []
          for(let i = 0; i <= level; i++) {
            pathArr.push(folders[i]);
          }

          let folder = pathArr.join("/");
          console.log(folder);
          let expectItem = list.find(item => {
              return item.folder === folder;
          });

          let treeItem = expectItem;
          treeItem.label = expectItem.name;
          console.log(treeItem);

          console.log(parentNode.children);
          parentNode.children = parentNode.children || [];
          let exist = parentNode.children.some((item) => {
            return item.folder === treeItem.folder;
          })
          console.log(exist);
          if(!exist) {
            // 如果不存在就加入tree
            parentNode.children.push(treeItem);
          }

          return;
        }

        let virtualItemName = folders[level];
        let treeItem = {};
        treeItem.id = 0;
        treeItem.label = virtualItemName;

        let pathArr = []
        for(let i = 1; i <= level; i++) {
          pathArr.push(folders[i]);
        }
        treeItem.folder = pathArr.join("/");

        parentNode.children = parentNode.children || [];
        let exist = parentNode.children.some((item) => {
          return item.folder === treeItem.folder;
        })

        if(!exist) {
          // 如果不存在就加入tree
          parentNode.children.push(treeItem);
        }

        level++;
        this.initChildren(tree, treeItem, maxLevel, level, folders, list);
      },
      addRootNodeToTree(tree, treeItem) {
        let expectItem = tree.find(item => {
          return item.folder === treeItem.folder;
        });

        if(expectItem == undefined) {
          // 如果不存在就加入tree
          tree.push(treeItem);
          return treeItem;
        }
        else {
          return expectItem;
        }
      },
      /**
       * 将传入的数据以树的形式展现到界面上
       * 传入的数据是扁平的，避免用户多了，计算压力叠在服务器上
       */
      makeTree(list) {
        // 按 categoryOrder 升序排序
        list.sort((a, b) => a.categoryOrder - b.categoryOrder);

        let tree = [];
        list.forEach((item) => {
          let firstMatchIdx = item.folder.indexOf("/");
          if (firstMatchIdx == -1) {
            /** 如果folder中不存在"/"，说明根叶子节点 */
            let treeItem = JSON.parse(JSON.stringify(item));
            treeItem.id = item.id || 0;
            treeItem.label = item.folder;
            treeItem.folder = item.folder;
            tree.push(treeItem);
            return;
          }
          /** 如果folder中存在"/"，说明是文件夹 */
          // 1/2/3 => virtualItemName 就是 1
          let virtualItemName = item.folder.substring(0, firstMatchIdx);
          let treeItem = {};
          treeItem.id = 0;
          treeItem.label = virtualItemName;
          treeItem.folder = virtualItemName;

          // 因为可能存在 1/2, 1/3 那么会有多个 1, 所以需要判断是否已经存在虚拟节点
          let exist = tree.some((item) => {
            return item.label === treeItem.label;
          })

          if(!exist) {
            // 如果不存在就加入tree
            tree.push(treeItem);
          }
        });

        /** 把根节点都找到后，开始往根节点添加子节点 */
        tree.forEach((item) => {
          this.findChild(item, list);
        });

        return tree;
      },
      findChild(parent, originList) {
        originList.forEach((item) => {
          if(parent.folder === item.folder) {
            /** originList是原始的数据list，所以可能重复 */
            return;
          }

          /**  根节点"1"，加上"/"， 所以前缀就是 "1/" */
          let prefix = parent.folder + "/";
          if (item.folder.startsWith(prefix)) {
            /** 如果 item.folder 以 "1/" 开头，说明是 1 的子节点 */
            let nestFolder = item.folder.substring(prefix.length);
            /** 1/2/3 ==> nestFolder 就是 2/3 */
            let firstMatchIdx = nestFolder.indexOf("/");
            /** 1/2/3 ==> nestFolder 就是 2/3 */
            if (firstMatchIdx != -1) {
              /** 处理多层结构 1/2/3， 如果是 2/3，说明还有虚拟节点 2 */
              let virtualItemName = nestFolder.substring(0, firstMatchIdx);
              let treeItem = {};
              treeItem.id = 0;
              treeItem.label = virtualItemName;
              treeItem.folder = prefix + virtualItemName;

              parent.children = parent.children || [];
              parent.children.push(treeItem);
              this.findChild(treeItem, originList);
              return;
            }

            /** 说明是当前循环元素是parent的子元素 */
            parent.children = parent.children || [];
            let treeItem = JSON.parse(JSON.stringify(item));
            treeItem.id = item.id || 0;
            treeItem.label = item.folder.substring((parent.label + "/").length);
            treeItem.folder = item.folder;

            parent.children.push(treeItem);
            this.findChild(treeItem, originList);
          }
        });
      },

    },

  }
</script>
