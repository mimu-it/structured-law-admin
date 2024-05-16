
export function makeTree2(list) {
    // 按 categoryOrder 升序排序
    list.sort((a, b) => a.categoryOrder - b.categoryOrder);

    let tree = [];
    list.forEach((item) => {
        let folders = item.folder.split("/");
        if (folders.length == 1) {
            /** 如果folder中不存在"/"，说明根叶子节点 */
            let treeItem = item;
            treeItem.label = item.name;

            addRootNodeToTree(tree, treeItem);
            return;
        }

        let virtualItemName = folders[0];
        let rootNode = {};
        rootNode.id = 0;
        rootNode.label = virtualItemName;
        rootNode.folder = virtualItemName;
        rootNode = addRootNodeToTree(tree, rootNode);

        initChildren(tree, rootNode, folders.length - 1, 1, folders, list);
    });

    return tree;
}

function initChildren(tree, parentNode, maxLevel, level, folders, list) {
    if (maxLevel === level) {
        //console.log(folders[maxLevel]);
        let pathArr = []
        for (let i = 0; i <= level; i++) {
            pathArr.push(folders[i]);
        }

        let folder = pathArr.join("/");
        console.log(folder);
        let expectItem = list.find(item => {
            return item.folder === folder;
        });

        let treeItem = expectItem;
        treeItem.label = expectItem.name;
        //console.log(treeItem);

        //console.log(parentNode.children);
        parentNode.children = parentNode.children || [];
        let exist = parentNode.children.some((item) => {
            return item.folder === treeItem.folder;
        })
        console.log(exist);
        if (!exist) {
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
    for (let i = 1; i <= level; i++) {
        pathArr.push(folders[i]);
    }
    treeItem.folder = pathArr.join("/");

    parentNode.children = parentNode.children || [];
    let exist = parentNode.children.some((item) => {
        return item.folder === treeItem.folder;
    })

    if (!exist) {
        // 如果不存在就加入tree
        parentNode.children.push(treeItem);
    }

    level++;
    initChildren(tree, treeItem, maxLevel, level, folders, list);
}

function addRootNodeToTree(tree, treeItem) {
    let expectItem = tree.find(item => {
        return item.folder === treeItem.folder;
    });

    if (expectItem == undefined) {
        // 如果不存在就加入tree
        tree.push(treeItem);
        return treeItem;
    } else {
        return expectItem;
    }
}
