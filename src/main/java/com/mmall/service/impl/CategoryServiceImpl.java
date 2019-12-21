package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.internal.$Gson$Types;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    CategoryMapper categoryMapper;

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(int categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);

        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到当前子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true);//这个分类是可用的

        int rowCount = categoryMapper.insert(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("更新品类名字成功");
        }
        return ServerResponse.createByErrorMessage("更新品类名字失败");
    }

/*    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer parentCategoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(parentCategoryId);
        List<Integer> list =  Lists.newArrayList();
        list.add(parentCategoryId);
        if (!categoryList.isEmpty()){
            for (Category category : categoryList) {
                list.add(category.getId());
            }
            return ServerResponse.createBySuccess(list);
        }else {
            return ServerResponse.createByErrorMessage("当前节点无子节点");
        }
    }*/

    /**
     * 递归查询本节点的id及孩子节点的id
     *
     * @param parentCategoryId
     * @return
     */
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer parentCategoryId) {
        List<Integer> categoryList = Lists.newArrayList();
        List<Integer> childCategory = findChildCategory(categoryList, parentCategoryId);
        if (childCategory==null){
            return ServerResponse.createByErrorMessage("传入节点参数有误或没有子节点");
        }
        return  ServerResponse.createBySuccess(childCategory);
    }
    //递归算法,算出子节点
    private List<Integer> findChildCategory(List<Integer> categoryList, Integer parentId) {
        List<Category> childrenList = categoryMapper.selectCategoryChildrenByParentId(parentId);
        if (!childrenList.isEmpty()) {
            for (Category child : childrenList) {
                categoryList.add(child.getId());
                findChildCategory(categoryList, child.getId());
            }
        } else {
            return null;
        }
        return categoryList;
    }
}
