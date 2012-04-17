<%--
  Tag: ui.avatar
  Usage: Render avatar image for given email address
  
  Available variables:
  
  classes - String of classes supplied
  avatarClass - Class to apply to the avatar element
  user - The email address of the user
  size - The size
  defaultSrc - an absolute URL to a default avatar image to use if none is found for the user
--%>
<img src="https://secure.gravatar.com/avatar/${user.encodeAsMD5()}?d=${defaultSrc?.encodeAsURL()}" 
    class="${p.joinClasses(values:[avatarClass,classes])}"${ui.attributes(exclude:'src')}/>
