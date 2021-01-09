package cn.stylefeng.roses.kernel.system.pojo.menu;

import cn.stylefeng.roses.kernel.rule.pojo.request.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * 系统菜单按钮请求参数
 *
 * @author q18idc.com QQ993143799
 * @date 2021/1/9 11:19
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SysMenuButtonRequest extends BaseRequest {

    /**
     * 主键
     */
    @NotNull(message = "按钮id不能为空，请检查buttonId参数", groups = {edit.class, detail.class, delete.class})
    private Long buttonId;

    /**
     * 菜单按钮主键集合
     */
    @NotEmpty(message = "菜单按钮主键集合不能为空，请检查 buttonIds 参数", groups = {batchDelete.class})
    private Set<@NotNull(message = "菜单按钮主键集合不能为空，请检查 buttonIds 参数", groups = {batchDelete.class}) Long> buttonIds;

    /**
     * 菜单id，按钮需要挂在菜单下
     */
    @NotNull(message = "菜单id不能为空，请检查menuId参数", groups = {add.class, edit.class, list.class})
    private Long menuId;

    /**
     * 按钮的名称
     */
    @NotBlank(message = "按钮名称不能为空，请检查buttonName参数", groups = {add.class, edit.class})
    private String buttonName;

    /**
     * 按钮的编码
     */
    @NotBlank(message = "按钮编码不能为空，请检查buttonCode参数", groups = {add.class, edit.class})
    private String buttonCode;

    /**
     * 批量删除验证分组
     */
    public @interface batchDelete {

    }

}
