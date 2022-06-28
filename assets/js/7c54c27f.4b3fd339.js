"use strict";(self.webpackChunkstarwhale_docs=self.webpackChunkstarwhale_docs||[]).push([[482],{3905:function(e,t,n){n.d(t,{Zo:function(){return p},kt:function(){return c}});var a=n(7294);function r(e,t,n){return t in e?Object.defineProperty(e,t,{value:n,enumerable:!0,configurable:!0,writable:!0}):e[t]=n,e}function l(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function i(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?l(Object(n),!0).forEach((function(t){r(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):l(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function o(e,t){if(null==e)return{};var n,a,r=function(e,t){if(null==e)return{};var n,a,r={},l=Object.keys(e);for(a=0;a<l.length;a++)n=l[a],t.indexOf(n)>=0||(r[n]=e[n]);return r}(e,t);if(Object.getOwnPropertySymbols){var l=Object.getOwnPropertySymbols(e);for(a=0;a<l.length;a++)n=l[a],t.indexOf(n)>=0||Object.prototype.propertyIsEnumerable.call(e,n)&&(r[n]=e[n])}return r}var m=a.createContext({}),u=function(e){var t=a.useContext(m),n=t;return e&&(n="function"==typeof e?e(t):i(i({},t),e)),n},p=function(e){var t=u(e.components);return a.createElement(m.Provider,{value:t},e.children)},d={inlineCode:"code",wrapper:function(e){var t=e.children;return a.createElement(a.Fragment,{},t)}},s=a.forwardRef((function(e,t){var n=e.components,r=e.mdxType,l=e.originalType,m=e.parentName,p=o(e,["components","mdxType","originalType","parentName"]),s=u(n),c=r,k=s["".concat(m,".").concat(c)]||s[c]||d[c]||l;return n?a.createElement(k,i(i({ref:t},p),{},{components:n})):a.createElement(k,i({ref:t},p))}));function c(e,t){var n=arguments,r=t&&t.mdxType;if("string"==typeof e||r){var l=n.length,i=new Array(l);i[0]=s;var o={};for(var m in t)hasOwnProperty.call(t,m)&&(o[m]=t[m]);o.originalType=e,o.mdxType="string"==typeof e?e:r,i[1]=o;for(var u=2;u<l;u++)i[u]=n[u];return a.createElement.apply(null,i)}return a.createElement.apply(null,n)}s.displayName="MDXCreateElement"},7233:function(e,t,n){n.r(t),n.d(t,{assets:function(){return p},contentTitle:function(){return m},default:function(){return c},frontMatter:function(){return o},metadata:function(){return u},toc:function(){return d}});var a=n(7462),r=n(3366),l=(n(7294),n(3905)),i=["components"],o={title:"Starwhale Runtime"},m=void 0,u={unversionedId:"standalone/runtime",id:"standalone/runtime",title:"Starwhale Runtime",description:"What is Starwhale Runtime?",source:"@site/docs/standalone/runtime.md",sourceDirName:"standalone",slug:"/standalone/runtime",permalink:"/docs/standalone/runtime",draft:!1,editUrl:"https://github.com/star-whale/starwhale/tree/main/docs/docs/standalone/runtime.md",tags:[],version:"current",frontMatter:{title:"Starwhale Runtime"},sidebar:"mainSidebar",previous:{title:"Overview",permalink:"/docs/standalone/overview"},next:{title:"Starwhale Model",permalink:"/docs/standalone/model"}},p={},d=[{value:"What is Starwhale Runtime?",id:"what-is-starwhale-runtime",level:2},{value:"RECOMMENDED Workflow",id:"recommended-workflow",level:2},{value:"1. Standalone Preparing Phase",id:"1-standalone-preparing-phase",level:3},{value:"2. Standalone Build and Share Phase",id:"2-standalone-build-and-share-phase",level:3},{value:"3. Use Pre-defined Runtime Phase",id:"3-use-pre-defined-runtime-phase",level:3},{value:"runtime.yaml Definition",id:"runtimeyaml-definition",level:2}],s={toc:d};function c(e){var t=e.components,n=(0,r.Z)(e,i);return(0,l.kt)("wrapper",(0,a.Z)({},s,n,{components:t,mdxType:"MDXLayout"}),(0,l.kt)("h2",{id:"what-is-starwhale-runtime"},"What is Starwhale Runtime?"),(0,l.kt)("p",null,"In the development of ML/DL, python is the first class language. A standard and easy-to-use python runtime environment is very important. Starwhale Runtime tries to provide an out-of-the-box runtime management tool that includes:"),(0,l.kt)("ul",null,(0,l.kt)("li",{parentName:"ul"},"a ",(0,l.kt)("inlineCode",{parentName:"li"},"runtime.yaml")," file"),(0,l.kt)("li",{parentName:"ul"},"some commands to finish runtime workflow"),(0,l.kt)("li",{parentName:"ul"},"a bundle file named ",(0,l.kt)("inlineCode",{parentName:"li"},".swrt")),(0,l.kt)("li",{parentName:"ul"},"runtime store in standalone and cloud instance")),(0,l.kt)("p",null,"When we use Starwhale Runtime, we can gain some devops abilities:"),(0,l.kt)("ul",null,(0,l.kt)("li",{parentName:"ul"},"versioning"),(0,l.kt)("li",{parentName:"ul"},"shareable"),(0,l.kt)("li",{parentName:"ul"},"reproducible and runnable"),(0,l.kt)("li",{parentName:"ul"},"system independent")),(0,l.kt)("h2",{id:"recommended-workflow"},"RECOMMENDED Workflow"),(0,l.kt)("h3",{id:"1-standalone-preparing-phase"},"1. Standalone Preparing Phase"),(0,l.kt)("ul",null,(0,l.kt)("li",{parentName:"ul"},"Step1: Create a runtime, command: ",(0,l.kt)("inlineCode",{parentName:"li"},"swcli runtime create --mode <venv|conda> --name <runtime name> --python <python version> WORKDIR")),(0,l.kt)("li",{parentName:"ul"},"Step2: Activate this runtime, command: ",(0,l.kt)("inlineCode",{parentName:"li"},"swcli runtime activate WORKDIR")),(0,l.kt)("li",{parentName:"ul"},"Step3: Install python requirements, use ",(0,l.kt)("inlineCode",{parentName:"li"},"pip install")," or ",(0,l.kt)("inlineCode",{parentName:"li"},"conda install"),"."),(0,l.kt)("li",{parentName:"ul"},"Step4: Test python environment: evaluate model, build dataset or run some python scripts.")),(0,l.kt)("h3",{id:"2-standalone-build-and-share-phase"},"2. Standalone Build and Share Phase"),(0,l.kt)("ul",null,(0,l.kt)("li",{parentName:"ul"},"Step1: Build a runtime, command: ",(0,l.kt)("inlineCode",{parentName:"li"},"swcli runtime build WORKDIR")),(0,l.kt)("li",{parentName:"ul"},"Step2: Run with runtime, command: ",(0,l.kt)("inlineCode",{parentName:"li"},"swcli job create --model mnist/version/latest --runtime pytorch-mnist/version/latest --dataset mnist/version/latest"),"."),(0,l.kt)("li",{parentName:"ul"},"Step3: Copy a runtime to cloud instance, command: ",(0,l.kt)("inlineCode",{parentName:"li"},"swcli runtime copy pytorch-mnist-env/version/latest http://<host>:<port>/project/self"),".")),(0,l.kt)("h3",{id:"3-use-pre-defined-runtime-phase"},"3. Use Pre-defined Runtime Phase"),(0,l.kt)("ul",null,(0,l.kt)("li",{parentName:"ul"},"Step1: Copy a runtime to standalone instance, command: ",(0,l.kt)("inlineCode",{parentName:"li"},"swcli runtime copy http://<host>:<port>/project/self/runtime/pytorch-mnist-env/version/latest local/project/self")),(0,l.kt)("li",{parentName:"ul"},"Step2: Restore runtime for develop: ",(0,l.kt)("inlineCode",{parentName:"li"},"swcli runtime restore mnist/version/latest"),"."),(0,l.kt)("li",{parentName:"ul"},"Step3: Run with runtime, same as Phase2-3.")),(0,l.kt)("h2",{id:"runtimeyaml-definition"},"runtime.yaml Definition"),(0,l.kt)("table",null,(0,l.kt)("thead",{parentName:"table"},(0,l.kt)("tr",{parentName:"thead"},(0,l.kt)("th",{parentName:"tr",align:null},"Name"),(0,l.kt)("th",{parentName:"tr",align:null},"Description"),(0,l.kt)("th",{parentName:"tr",align:null},"Required"),(0,l.kt)("th",{parentName:"tr",align:null},"Default Value"),(0,l.kt)("th",{parentName:"tr",align:null},"Example"))),(0,l.kt)("tbody",{parentName:"table"},(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"mode"),(0,l.kt)("td",{parentName:"tr",align:null},"environment mode, venv or conda"),(0,l.kt)("td",{parentName:"tr",align:null},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"venv")),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"venv"))),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"name"),(0,l.kt)("td",{parentName:"tr",align:null},"runtime name"),(0,l.kt)("td",{parentName:"tr",align:null},"\u2705"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},'""')),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"pytorch-mnist"))),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"pip_req"),(0,l.kt)("td",{parentName:"tr",align:null},"the path of requirements.txt"),(0,l.kt)("td",{parentName:"tr",align:null},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"requirements.txt")),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"requirements.txt"))),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"python_version"),(0,l.kt)("td",{parentName:"tr",align:null},"python version, format is major:minor"),(0,l.kt)("td",{parentName:"tr",align:null},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"3.8")),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"3.9"))),(0,l.kt)("tr",{parentName:"tbody"},(0,l.kt)("td",{parentName:"tr",align:null},"starwhale_version"),(0,l.kt)("td",{parentName:"tr",align:null},"starwhale python package version"),(0,l.kt)("td",{parentName:"tr",align:null},"\u274c"),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},'""')),(0,l.kt)("td",{parentName:"tr",align:null},(0,l.kt)("inlineCode",{parentName:"td"},"0.2.0b20"))))),(0,l.kt)("p",null,"Example:"),(0,l.kt)("pre",null,(0,l.kt)("code",{parentName:"pre",className:"language-yaml"},"mode: venv\nname: pytorch-mnist\npip_req: requirements.txt\npython_version: '3.8'\nstarwhale_version: '0.2.0b20'\n")),(0,l.kt)("ul",null,(0,l.kt)("li",{parentName:"ul"},(0,l.kt)("inlineCode",{parentName:"li"},"swcli runtime create")," command creates a ",(0,l.kt)("inlineCode",{parentName:"li"},"runtime.yaml")," in the workdir which is a RECOMMENDED method."),(0,l.kt)("li",{parentName:"ul"},(0,l.kt)("inlineCode",{parentName:"li"},"swcli")," uses ",(0,l.kt)("inlineCode",{parentName:"li"},"starwhale_version")," version to render docker image.")))}c.isMDXComponent=!0}}]);