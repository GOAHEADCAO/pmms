package com.dugstudio.pmms.shiro;

import com.dugstudio.pmms.entity.Resource;
import com.dugstudio.pmms.entity.Role;
import com.dugstudio.pmms.entity.User;
import com.dugstudio.pmms.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

public class ShiroDBRealm extends AuthorizingRealm {
    @Autowired
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username=(String)principals.getPrimaryPrincipal();
        System.out.println("doGetAuthorizationInfo :username:"+username);
        User user=userService.findUserBySno(username);
        if(user!=null){
            System.out.println("user :"+user);
            Role role=user.getRole();
            if(role!=null){
                System.out.println("user.getRole:  "+role);
                SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
                info.addRole(user.getRole().getName());
                Set<Resource> resources=role.getResource();
                if(resources.size()>0&&resources!=null){
                    for (Resource resource:resources){
                        info.addStringPermission(resource.getName());
                        System.out.println("resource.getName():"+resource.getName());
                    }
                    return info;
                }else{
                    System.out.println("用户："+user.getSno()+"拥有角色"+user.getRole()+" 没有权限");
                }
                return info;
            }else{
                System.out.println(user.getSno()+"user.getRole=null");
            }
        }
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        System.out.println("doGetAuthenticationInfo-------------------------");
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        System.out.println(token.getUsername());
        if (StringUtils.isEmpty(token.getUsername())) {
            return null;
        }
        System.out.println(token.getUsername());
        User user = userService.findUserBySno(token.getUsername());
        if (user != null) {
            if (user.getStatus() != 1) {
                throw new LockedAccountException();
            }
            AuthenticationInfo authcInfo = new SimpleAuthenticationInfo(user.getSno(), user.getPassword(),getName());
            setSession("user", user);
            setSession("msg","登陆成功");
            System.out.println(user.getStatus()+"yanzhengchengg"+getName()+"8888"+user.getSno()+user.getPassword());
            return authcInfo;
        }
        System.out.println("验证失败");
        return null;
    }
    private void setSession(Object key, Object value) {
        Subject subject = SecurityUtils.getSubject();
        if (subject != null) {
            Session session = subject.getSession();
            if (session != null) {
                session.setAttribute(key, value);
            }
        }
    }
}
