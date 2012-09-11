<p:callTag tag="p:button" class="${p.joinClasses(values:[buttonClass,classes,mode])}" 
    kind="${kind}" attrs="${attrs + (disabled ? [disabled:true] : [:])}" bodyContent="${text}"/>
