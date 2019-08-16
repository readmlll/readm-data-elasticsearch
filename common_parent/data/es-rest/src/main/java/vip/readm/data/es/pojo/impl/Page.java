package vip.readm.data.es.pojo.impl;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * @Author: Readm
 * @Date: 2019/8/15 17:31
 * @Version 1.0
 */


/**
 * 封装 分页请求、分页返回对象
 *
 * 分页说明  传入的当前页数第一页规定为0   ，  传入的当前页数 小于0自动归为第一页
 *
 *        //分页设置 如果没有分页信息 自动分页为 第一页  10条数据
 *
 * @param <T>
 */

@Accessors(chain = true)
@EqualsAndHashCode
@ToString
public class Page<T> extends AbstractPageRequest implements org.springframework.data.domain.Page<T> {


    public Page(Integer curPage,Integer pageSize,Sort sort){

        //返回分页请求用

        //即便传入错误的参数给父类也没有关系 多态保证了调用还是会初始化参数
        super(curPage,pageSize);
        initPageFieldCommon(curPage,pageSize,false);
        this.sort=sort;
    }

    public Page(Integer curPage,Integer pageSize,Long eleTotal,List<T> datas){

        //返回分页数据用
        super(curPage,pageSize);
        this.content=datas;
        this.eleTotal=eleTotal;
        initPageFieldCommon(curPage,pageSize,true);

    }

    public void initPageFieldCommon(Integer curPage,Integer pageSize,boolean initComputeField){

        if(pageSize==null || pageSize<=0){
            pageSize=10;
        }

        if(curPage==null||curPage<0){
            curPage=0;
            //保证传入的页码为描述所说从0开始。
        }
        this.curPageNum=curPage;
        this.pageSize=pageSize;


        //初始化计算属性
        if(initComputeField){

            Long pageTotal=eleTotal/pageSize;
            pageTotal=eleTotal%pageSize==0?pageTotal:pageTotal+1;
            this.pageTotal= pageTotal.intValue();

            this.first=(curPage==0);

            this.last=((curPage+1)==this.pageTotal);

        }

    }

    //需要传入
    Long eleTotal;  // 数据总数
    Integer curPageNum; //当前在第几页
    Integer pageSize;   //每页多少数据
    List<T> content; // 当前页数据集合
    Sort sort; //排序


    //类自动计算得出
    Integer pageTotal; //一共多少页
    boolean first;  //是否是第一页
    boolean last;  //是否是最后一页






    public int getPageSize() {
        return this.pageSize;
    }


    public int getPageNumber() {

        return this.curPageNum;
    }


    public int getTotalPages() {
        return this.pageTotal;
    }


    public long getTotalElements() {
        return this.eleTotal;
    }



    public int getNumber() {
        return this.curPageNum;
    }


    public int getSize() {
        return this.pageSize;
    }


    public int getNumberOfElements() {

        int size=0;
        try {
            this.content.size();
        }catch (Exception e){
        }
        return size;
    }


    public List<T> getContent() {
        return this.content;
    }


    public boolean hasContent() {
        return this.content!=null;
    }



    public boolean isFirst() {
        return this.first;
    }


    public boolean isLast() {
        return this.last;
    }


    public boolean hasNext() {

        return this.last!=true;
    }

    @Override
    public boolean hasPrevious() {
        return this.first!=true;
    }


    public Iterator<T> iterator() {
        return this.content.iterator();
    }



    @Override
    public Sort getSort() {
        return this.sort;
    }





    //未实现

    @Deprecated
    public <U> Page<U>  map(Function<? super T, ? extends U> converter) {

        return null;
    }

    @Deprecated
    public Pageable nextPageable() {
        return null;
    }

    @Deprecated
    public Pageable previousPageable() {
        return null;
    }


    @Override
    @Deprecated
    public boolean isPaged() {
        return false;
    }

    @Override
    @Deprecated
    public boolean isUnpaged() {
        return false;
    }

    @Override
    @Deprecated
    public Sort getSortOr(Sort sort) {
        return this.sort;
    }

    @Override
    @Deprecated
    public Pageable next() {
        return null;
    }

    @Override
    @Deprecated
    public Pageable previous() {
        return null;
    }

    @Override
    @Deprecated
    public Pageable first() {
        return null;
    }

    @Override
    @Deprecated
    public Optional<Pageable> toOptional() {
        return Optional.empty();
    }
}
