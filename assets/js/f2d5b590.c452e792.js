"use strict";(self.webpackChunkstarwhale_docs=self.webpackChunkstarwhale_docs||[]).push([[437],{3905:function(e,t,n){n.d(t,{Zo:function(){return u},kt:function(){return m}});var r=n(7294);function a(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function i(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function o(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?i(Object(n),!0).forEach((function(t){a(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):i(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function s(e,t){if(null==e)return{};var n,r,a=function(e,t){if(null==e)return{};var n,r,a={},i=Object.keys(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||(a[n]=e[n]);return a}(e,t);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)n=i[r],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(a[n]=e[n])}return a}var l=r.createContext({}),c=function(e){var t=r.useContext(l),n=t;return e&&(n="function"==typeof e?e(t):o(o({},t),e)),n},u=function(e){var t=c(e.components);return r.createElement(l.Provider,{value:t},e.children)},p={inlineCode:"code",wrapper:function(e){var t=e.children;return r.createElement(r.Fragment,{},t)}},d=r.forwardRef((function(e,t){var n=e.components,a=e.mdxType,i=e.originalType,l=e.parentName,u=s(e,["components","mdxType","originalType","parentName"]),d=c(n),m=a,f=d["".concat(l,".").concat(m)]||d[m]||p[m]||i;return n?r.createElement(f,o(o({ref:t},u),{},{components:n})):r.createElement(f,o({ref:t},u))}));function m(e,t){var n=arguments,a=t&&t.mdxType;if("string"==typeof e||a){var i=n.length,o=new Array(i);o[0]=d;var s={};for(var l in t)hasOwnProperty.call(t,l)&&(s[l]=t[l]);s.originalType=e,s.mdxType="string"==typeof e?e:a,o[1]=s;for(var c=2;c<i;c++)o[c]=n[c];return r.createElement.apply(null,o)}return r.createElement.apply(null,n)}d.displayName="MDXCreateElement"},6708:function(e,t,n){n.r(t),n.d(t,{assets:function(){return u},contentTitle:function(){return l},default:function(){return m},frontMatter:function(){return s},metadata:function(){return c},toc:function(){return p}});var r=n(7462),a=n(3366),i=(n(7294),n(3905)),o=["components"],s={title:"On-Premises Quickstart"},l=void 0,c={unversionedId:"quickstart/on-premises",id:"quickstart/on-premises",title:"On-Premises Quickstart",description:"We recommend you read and practice standalone quickstart first.",source:"@site/docs/quickstart/on-premises.md",sourceDirName:"quickstart",slug:"/quickstart/on-premises",permalink:"/docs/quickstart/on-premises",draft:!1,editUrl:"https://github.com/star-whale/starwhale/tree/main/docs/docs/quickstart/on-premises.md",tags:[],version:"current",frontMatter:{title:"On-Premises Quickstart"},sidebar:"mainSidebar",previous:{title:"Standalone Quickstart",permalink:"/docs/quickstart/standalone"},next:{title:"main",permalink:"/docs/tutorials/main"}},u={},p=[{value:"Installing Standalone",id:"installing-standalone",level:2},{value:"Kubernetes",id:"kubernetes",level:3},{value:"Bare Metal",id:"bare-metal",level:3}],d={toc:p};function m(e){var t=e.components,n=(0,a.Z)(e,o);return(0,i.kt)("wrapper",(0,r.Z)({},d,n,{components:t,mdxType:"MDXLayout"}),(0,i.kt)("div",{className:"admonition admonition-tip alert alert--success"},(0,i.kt)("div",{parentName:"div",className:"admonition-heading"},(0,i.kt)("h5",{parentName:"div"},(0,i.kt)("span",{parentName:"h5",className:"admonition-icon"},(0,i.kt)("svg",{parentName:"span",xmlns:"http://www.w3.org/2000/svg",width:"12",height:"16",viewBox:"0 0 12 16"},(0,i.kt)("path",{parentName:"svg",fillRule:"evenodd",d:"M6.5 0C3.48 0 1 2.19 1 5c0 .92.55 2.25 1 3 1.34 2.25 1.78 2.78 2 4v1h5v-1c.22-1.22.66-1.75 2-4 .45-.75 1-2.08 1-3 0-2.81-2.48-5-5.5-5zm3.64 7.48c-.25.44-.47.8-.67 1.11-.86 1.41-1.25 2.06-1.45 3.23-.02.05-.02.11-.02.17H5c0-.06 0-.13-.02-.17-.2-1.17-.59-1.83-1.45-3.23-.2-.31-.42-.67-.67-1.11C2.44 6.78 2 5.65 2 5c0-2.2 2.02-4 4.5-4 1.22 0 2.36.42 3.22 1.19C10.55 2.94 11 3.94 11 5c0 .66-.44 1.78-.86 2.48zM4 14h5c-.23 1.14-1.3 2-2.5 2s-2.27-.86-2.5-2z"}))),"Disabling Automatic Logging")),(0,i.kt)("div",{parentName:"div",className:"admonition-content"},(0,i.kt)("p",{parentName:"div"},"We recommend you read and practice ",(0,i.kt)("a",{parentName:"p",href:"/docs/quickstart/standalone"},"standalone quickstart")," first."))),(0,i.kt)("h2",{id:"installing-standalone"},"Installing Standalone"),(0,i.kt)("p",null,"Starwhale provides two ways to install in your private cluster:"),(0,i.kt)("ul",null,(0,i.kt)("li",{parentName:"ul"},"For Kubernetes: a pre-deployed kubernetes cluster is the precondition."),(0,i.kt)("li",{parentName:"ul"},"For Bare Metal: host machine should install docker(>=19.03), nvidia-docker-plugin(optional).")),(0,i.kt)("h3",{id:"kubernetes"},"Kubernetes"),(0,i.kt)("p",null,"We need Kubernetes 1.19+ and Helm 3.2.0+."),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-bash"},"helm repo add starwhale https://star-whale.github.io/charts\nhelm repo update\nhelm install starwhale starwhale/starwhale -n starwhale --create-namespace\n")),(0,i.kt)("p",null,"You can get more details from the ",(0,i.kt)("a",{parentName:"p",href:"/docs/cloud/helm-charts"},"doc"),"."),(0,i.kt)("h3",{id:"bare-metal"},"Bare Metal"))}m.isMDXComponent=!0}}]);