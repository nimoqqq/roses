package cn.stylefeng.roses.kernel.db.init.actuator;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.stylefeng.roses.kernel.db.api.exception.enums.DbInitEnum;
import cn.stylefeng.roses.kernel.db.init.util.SqlExe;
import cn.stylefeng.roses.kernel.rule.exception.base.ServiceException;
import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 数据库初始化，可初始化表，校验字段，校验表名是否存在等
 *
 * @author fengshuonan
 * @date 2018-07-29 22:05
 */
@Slf4j
@Getter
@Setter
public abstract class DbInitializer {

    /**
     * 如果为true，则数据库校验失败会抛出异常
     */
    private Boolean fieldValidatorExceptionFlag = true;

    public DbInitializer() {

    }

    public DbInitializer(Boolean fieldValidatorExceptionFlag) {
        this.fieldValidatorExceptionFlag = fieldValidatorExceptionFlag;
    }

    /**
     * 初始化数据库
     *
     * @author fengshuonan
     * @date 2018/7/30 上午10:30
     */
    public void dbInit() {

        /**
         * 初始化表
         */
        initTable();

        /**
         * 校验实体和对应表结构是否有不一致的
         */
        fieldsValidate();
    }

    /**
     * 初始化表结构
     *
     * @author fengshuonan
     * @date 2018/7/30 上午10:24
     */
    private void initTable() {

        //校验参数
        String tableName = this.getTableName();
        String tableInitSql = this.getTableInitSql();
        if (ObjectUtil.isEmpty(tableName) || ObjectUtil.isEmpty(tableInitSql)) {
            if (fieldValidatorExceptionFlag) {
                throw new ServiceException(DbInitEnum.INIT_TABLE_EMPTY_PARAMS);
            }
        }

        //列出数据库中所有的表
        List<Map<String, Object>> tables = SqlExe.selectList("SHOW TABLES");
        boolean haveSmsTableFlag = false;
        for (Map<String, Object> tableInfo : tables) {
            if (tableInfo.containsValue(tableName.toUpperCase()) || tableInfo.containsValue(tableName.toLowerCase())) {
                haveSmsTableFlag = true;
            }
        }

        //判断数据库中是否有这张表，如果没有就初始化
        if (!haveSmsTableFlag) {
            SqlExe.update(tableInitSql);
            log.info("初始化" + getTableName() + "成功！");
        }

    }

    /**
     * 校验实体和对应表结构是否有不一致的
     *
     * @author fengshuonan
     * @date 2018/7/30 上午10:24
     */
    private void fieldsValidate() {

        //校验参数
        String sql = this.showColumnsSql();
        if (ObjectUtil.isEmpty(sql)) {
            if (fieldValidatorExceptionFlag) {
                throw new ServiceException(DbInitEnum.INIT_TABLE_EMPTY_PARAMS);
            }
        }

        //检查数据库中的字段，是否和实体字段一致
        List<Map<String, Object>> tableFields = SqlExe.selectList(sql);
        if (tableFields != null && !tableFields.isEmpty()) {

            //用于保存实体中不存在的字段的名称集合
            List<String> fieldsNotInClass = new ArrayList<>();

            //反射获取字段的所有字段名称
            List<String> classFields = this.getClassFields();
            for (Map<String, Object> tableField : tableFields) {
                String fieldName = (String) tableField.get("COLUMN_NAME");
                if (fieldName == null) {
                    fieldName = (String) tableField.get("Field");
                }
                if (!classFields.contains(fieldName.toLowerCase())) {
                    fieldsNotInClass.add(fieldName);
                }
            }

            //如果集合不为空，代表有实体和数据库不一致的数据
            if (!fieldsNotInClass.isEmpty()) {
                log.error("实体中和数据库字段不一致的字段如下：" + JSON.toJSONString(fieldsNotInClass));
                if (fieldValidatorExceptionFlag) {
                    throw new ServiceException(DbInitEnum.FIELD_VALIDATE_ERROR);
                }
            }
        }
    }

    /**
     * 反射获取类的所有字段
     *
     * @author fengshuonan
     * @date 2018/7/30 上午10:06
     */
    private List<String> getClassFields() {
        Class<?> entityClass = this.getEntityClass();
        Field[] declaredFields = ClassUtil.getDeclaredFields(entityClass);
        ArrayList<String> filedNamesUnderlineCase = new ArrayList<>();
        for (Field declaredField : declaredFields) {
            String fieldName = StrUtil.toUnderlineCase(declaredField.getName());
            filedNamesUnderlineCase.add(fieldName);
        }

        // 获取父类的所有字段名称
        Field[] superfields = ReflectUtil.getFields(entityClass.getSuperclass());
        for (Field superfield : superfields) {
            String fieldName = StrUtil.toUnderlineCase(superfield.getName());
            filedNamesUnderlineCase.add(fieldName);
        }

        return filedNamesUnderlineCase;
    }

    /**
     * 获取表的字段
     *
     * @author stylefeng
     * @date 2018/7/29 22:49
     */
    private String showColumnsSql() {
        return "SHOW COLUMNS FROM " + this.getTableName();
    }

    /**
     * 获取表的初始化语句
     *
     * @author stylefeng
     * @date 2018/7/29 22:10
     */
    protected abstract String getTableInitSql();

    /**
     * 获取表的名称
     *
     * @author stylefeng
     * @date 2018/7/29 22:10
     */
    protected abstract String getTableName();

    /**
     * 获取表对应的实体
     *
     * @author stylefeng
     * @date 2018/7/29 22:49
     */
    protected abstract Class<?> getEntityClass();
    
}