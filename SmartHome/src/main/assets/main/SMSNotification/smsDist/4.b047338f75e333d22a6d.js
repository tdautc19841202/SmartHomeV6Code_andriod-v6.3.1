webpackJsonp([4],{DkI5:function(e,t,s){t=e.exports=s("FZ+f")(!1),t.push([e.i,"\n.message[data-v-0b113b84] {\n  width: 90%;\n  font-size: 18px;\n  display: block;\n  margin: 20px auto 0 auto;\n}\n",""])},G6VV:function(e,t,s){"use strict";function o(e){s("Q6C9")}Object.defineProperty(t,"__esModule",{value:!0});var n=s("vRg9"),i=s("r1Vi"),r={components:{usersList:n.a,headtop:i.a},data:function(){return{users:[]}},computed:{title:function(){return"sys"==this.$route.params.viewtype?this.$t("maincelltitle2"):this.$t("maincelltitle4")},message:function(){return"sys"==this.$route.params.viewtype?this.$t("sysSmsDesc"):this.$t("ringSmsDesc")},msgCode:function(){if("sys"==this.$route.params.viewtype)return"0103011";var e=this.$store.getters.devType;return console.log("devType -- "+e),"Bc"==e||"Bn"==e?"0103061":"0103081"}},mounted:function(){console.log("dev 2 "+this.$store.getters.deviceID);var e=this;window.plus.tools.getSmsPhone({messageCode:this.msgCode},function(t){console.log(t),0==t.resultCode&&void 0!=t.data.audience&&(e.users=t.data.audience)})},methods:{saveAction:function(){var e=this,t=this.msgCode;console.log(this.users),window.plus.tools.setSmsPhone({messageCode:t,phone:this.users.join(",")},function(t){console.log(t),e.$router.go(-1)})},backAction:function(){this.$router.go(-1)}}},a=function(){var e=this,t=e.$createElement,s=e._self._c||t;return s("div",[s("headtop",{attrs:{title:e.title,moreStr:e.$t("confirm")},on:{moreAction:e.saveAction,backAction:e.backAction}}),e._v(" "),s("div",[s("p",{staticClass:"message"},[e._v(e._s(e.message))])]),e._v(" "),s("usersList",{model:{value:e.users,callback:function(t){e.users=t},expression:"users"}})],1)},c=[],u={render:a,staticRenderFns:c},l=u,d=s("VU/8"),m=o,p=d(r,l,!1,m,"data-v-0b113b84",null);t.default=p.exports},Q6C9:function(e,t,s){var o=s("DkI5");"string"==typeof o&&(o=[[e.i,o,""]]),o.locals&&(e.exports=o.locals);s("rjj0")("708ea528",o,!0)}});