package cn.stylefeng.roses.kernel.dict.modular.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.stylefeng.roses.kernel.auth.api.context.LoginContext;
import cn.stylefeng.roses.kernel.db.api.factory.PageFactory;
import cn.stylefeng.roses.kernel.db.api.factory.PageResultFactory;
import cn.stylefeng.roses.kernel.db.api.pojo.page.PageResult;
import cn.stylefeng.roses.kernel.dict.api.constants.DictConstants;
import cn.stylefeng.roses.kernel.dict.api.exception.DictException;
import cn.stylefeng.roses.kernel.dict.api.exception.enums.DictExceptionEnum;
import cn.stylefeng.roses.kernel.dict.api.pojo.dict.request.ParentIdsUpdateRequest;
import cn.stylefeng.roses.kernel.dict.modular.entity.SysDict;
import cn.stylefeng.roses.kernel.dict.modular.entity.SysDictType;
import cn.stylefeng.roses.kernel.dict.modular.mapper.DictMapper;
import cn.stylefeng.roses.kernel.dict.modular.pojo.TreeDictInfo;
import cn.stylefeng.roses.kernel.dict.modular.pojo.request.DictRequest;
import cn.stylefeng.roses.kernel.dict.modular.service.DictService;
import cn.stylefeng.roses.kernel.pinyin.api.PinYinApi;
import cn.stylefeng.roses.kernel.rule.constants.SymbolConstant;
import cn.stylefeng.roses.kernel.rule.constants.TreeConstants;
import cn.stylefeng.roses.kernel.rule.enums.StatusEnum;
import cn.stylefeng.roses.kernel.rule.enums.YesOrNotEnum;
import cn.stylefeng.roses.kernel.rule.pojo.dict.SimpleDict;
import cn.stylefeng.roses.kernel.rule.tree.factory.DefaultTreeBuildFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 基础字典 服务实现类
 *
 * @author fengshuonan
 * @date 2020/12/26 22:36
 */
@Service
@Slf4j
public class DictServiceImpl extends ServiceImpl<DictMapper, SysDict> implements DictService {

    @Resource
    private PinYinApi pinYinApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(DictRequest dictRequest) {
        SysDict sysDict = new SysDict();
        BeanUtil.copyProperties(dictRequest, sysDict);
        sysDict.setDictParentId(DictConstants.DEFAULT_DICT_PARENT_ID);
        sysDict.setDictPids(StrUtil.BRACKET_START + DictConstants.DEFAULT_DICT_PARENT_ID + StrUtil.BRACKET_END + StrUtil.COMMA);
        sysDict.setStatusFlag(StatusEnum.ENABLE.getCode());
        sysDict.setDictNamePinyin(pinYinApi.parseEveryPinyinFirstLetter(sysDict.getDictName()));
        this.save(sysDict);
    }

    @Override
    public void del(DictRequest dictRequest) {
        SysDict sysDict = this.querySysDict(dictRequest);
        sysDict.setDelFlag(YesOrNotEnum.Y.getCode());
        this.updateById(sysDict);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void edit(DictRequest dictRequest) {

        SysDict sysDict = this.querySysDict(dictRequest);
        BeanUtil.copyProperties(dictRequest, sysDict);

        // 不能修改字典类型和编码
        sysDict.setDictTypeCode(null);
        sysDict.setDictCode(null);
        sysDict.setDictNamePinyin(pinYinApi.parseEveryPinyinFirstLetter(sysDict.getDictName()));

        this.updateById(sysDict);
    }

    @Override
    public SysDict detail(DictRequest dictRequest) {
        return this.getOne(this.createWrapper(dictRequest), false);
    }

    @Override
    public List<SysDict> findList(DictRequest dictRequest) {
        return this.list(this.createWrapper(dictRequest));
    }

    @Override
    public PageResult<SysDict> findPage(DictRequest dictRequest) {
        Page<SysDict> page = this.page(PageFactory.defaultPage(), this.createWrapper(dictRequest));
        return PageResultFactory.createPageResult(page);
    }


    @Override
    public List<TreeDictInfo> getTreeDictList(DictRequest dictRequest) {

        // 获取字典类型下所有的字典
        List<SysDict> sysDictList = this.findList(dictRequest);
        if (sysDictList == null || sysDictList.isEmpty()) {
            return new ArrayList<>();
        }

        // 构造树节点信息
        ArrayList<TreeDictInfo> treeDictInfos = new ArrayList<>();
        for (SysDict sysDict : sysDictList) {
            TreeDictInfo treeDictInfo = new TreeDictInfo();
            treeDictInfo.setDictId(sysDict.getDictId());
            treeDictInfo.setDictCode(sysDict.getDictCode());
            treeDictInfo.setDictParentId(sysDict.getDictParentId());
            treeDictInfo.setDictName(sysDict.getDictName());
            treeDictInfos.add(treeDictInfo);
        }

        // 构建菜单树
        return new DefaultTreeBuildFactory<TreeDictInfo>().doTreeBuild(treeDictInfos);
    }

    @Override
    public String getDictName(String dictTypeCode, String dictCode) {
        LambdaQueryWrapper<SysDict> sysDictLambdaQueryWrapper = new LambdaQueryWrapper<>();
        sysDictLambdaQueryWrapper.eq(SysDict::getDictTypeCode, dictTypeCode);
        sysDictLambdaQueryWrapper.eq(SysDict::getDictCode, dictCode);
        sysDictLambdaQueryWrapper.ne(SysDict::getDelFlag, YesOrNotEnum.Y.getCode());

        List<SysDict> list = this.list(sysDictLambdaQueryWrapper);

        // 如果查询不到字典，则返回空串
        if (list.isEmpty()) {
            return StrUtil.EMPTY;
        }

        // 字典code存在多个重复的，返回空串并打印错误日志
        if (list.size() > 1) {
            log.error(DictExceptionEnum.DICT_CODE_REPEAT.getUserTip(), "", dictCode);
            return StrUtil.EMPTY;
        }

        String dictName = list.get(0).getDictName();
        if (dictName != null) {
            return dictName;
        } else {
            return StrUtil.EMPTY;
        }
    }

    @Override
    public List<SimpleDict> getDictDetailsByDictTypeCode(String dictTypeCode) {
        DictRequest dictRequest = new DictRequest();
        dictRequest.setDictTypeCode(dictTypeCode);
        LambdaQueryWrapper<SysDict> wrapper = createWrapper(dictRequest);
        List<SysDict> dictList = this.list(wrapper);
        if (dictList.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<SimpleDict> simpleDictList = new ArrayList<>();
        for (SysDict sysDict : dictList) {
            SimpleDict simpleDict = new SimpleDict();
            simpleDict.setCode(sysDict.getDictCode());
            simpleDict.setName(sysDict.getDictName());
            simpleDictList.add(simpleDict);
        }
        return simpleDictList;
    }

    @Override
    public void deleteByDictId(Long dictId) {
        this.removeById(dictId);
    }

    /**
     * 获取详细信息
     *
     * @author chenjinlong
     * @date 2021/1/13 10:50
     */
    private SysDict querySysDict(DictRequest dictRequest) {
        SysDict sysDict = this.getById(dictRequest.getDictId());
        if (ObjectUtil.isNull(sysDict)) {
            throw new DictException(DictExceptionEnum.DICT_NOT_EXISTED, dictRequest.getDictId());
        }
        return sysDict;
    }

    /**
     * 构建wrapper
     *
     * @author chenjinlong
     * @date 2021/1/13 10:50
     */
    private LambdaQueryWrapper<SysDict> createWrapper(DictRequest dictRequest) {
        LambdaQueryWrapper<SysDict> queryWrapper = new LambdaQueryWrapper<>();

        // SQL拼接
        queryWrapper.eq(ObjectUtil.isNotNull(dictRequest.getDictId()), SysDict::getDictId, dictRequest.getDictId());
        queryWrapper.eq(StrUtil.isNotBlank(dictRequest.getDictTypeCode()), SysDict::getDictTypeCode, dictRequest.getDictTypeCode());
        queryWrapper.eq(StrUtil.isNotBlank(dictRequest.getDictCode()), SysDict::getDictCode, dictRequest.getDictCode());
        queryWrapper.like(StrUtil.isNotBlank(dictRequest.getDictName()), SysDict::getDictName, dictRequest.getDictName());

        queryWrapper.ne(SysDict::getDelFlag, YesOrNotEnum.Y.getCode());
        return queryWrapper;
    }

}
