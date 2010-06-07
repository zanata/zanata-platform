#!/bin/sh
# Import the sample projects

source ./test.cfg
NEED_NEW_APIKEY=1
FLIES_PUBLICAN_LOG=files_publican.log

function FIND_PROGRAM(){
   _cmd=`which $1 2>/dev/null`
   if [ -z "${_cmd}" ] ; then
       echo "Error: $1 cannot be found in path!"
       exit 1
   fi
   echo ${_cmd}
}

function set_opts(){
    export FLIES_PUBLICAN_COMMON_OPTS="--errors --debug --user admin --key ${APIKEY}"
}

function upload_(){
    echo "       Uploading project"
    _proj=$1
    _upload_dest="${FLIES_URL}/${REST_PATH}${_proj}/iterations/i/${INIT_ITER}/documents"
    echo "_upload_dest=${_upload_dest}"
    ${FLIES_PUBLICAN_CMD} upload ${FLIES_PUBLICAN_COMMON_OPTS} -i --src . --dst "${_upload_dest}" >> ${FLIES_PUBLICAN_LOG}
}

# get_projects <cachefile>
function get_projects(){
   curl --silent --output $1 ${FLIES_URL}/
}

# has_project <proj_id> <cachefile>
function has_project(){
   _res=`grep "<a href=\"/flies/project/view/$1" $2`
   if [ -z "${_res}" ]; then
       echo "FALSE"
   else
       echo "TRUE"
   fi
}

PUBLICAN_CMD=`FIND_PROGRAM publican`
FLIES_PUBLICAN_CMD=`FIND_PROGRAM flies-publican`
mkdir -p ${SAMPLE_PROJ_DIR}
rm -f ${FILES_PUBLICAN_LOG}
ACTION=$1
get_projects tmp0.html

for pProj in $PUBLICAN_PROJECTS; do
    _proj=$(eval echo \$${pProj})

    echo "Processing project ${_proj}:${_proj_name}"

    _flies_has_proj=`has_project ${_proj} tmp0.html`
    #echo "_flies_has_proj=${_flies_has_proj}"
    if [ "${_flies_has_proj}" = "TRUE" ]; then
	if [ -z ${ACTION} ]; then
	    echo "  Flies already has this project, skip importing."
	    continue
	fi
	echo "  Flies has this project, start ${ACTION}."
	APIKEY=`cat apikey`
	NEED_NEW_APIKEY=0
    else
	echo "  Flies does not have this project, start importing."
    fi

    # Reload current apikey
    if [ ${NEED_NEW_APIKEY} -eq 1 ];then
        echo "Reload current apikey"
	NEED_NEW_APIKEY=0
	source ./get_apikey.sh ${FLIES_URL}
    fi
    set_opts

    _clone_action=
    _update_action=
    _repo_cmd=$(eval echo \$${pProj}_REPO_TYPE)
    if [ "${_repo_cmd}" = "git" ]; then
	_clone_action="clone"
	_update_action="pull"
    elif [ "${_repo_cmd}" = "svn" ]; then
	_clone_action="co"
	_update_action="up"
    fi

    _proj_dir="${SAMPLE_PROJ_DIR}/${_proj}"

    if [ -z ${ACTION} ]; then
	if [ ! -d "${_proj_dir}" ]; then
	    echo "    ${_proj_dir} does not exist, clone now."
	    _proj_url=$(eval echo \$${pProj}_URL)
	    ${_repo_cmd} "${_clone_action}" "${_proj_url}" "${_proj_dir}"
	else
	    echo "    ${_proj_dir} exists, updating."
	    (cd ${_proj_dir}; ${_repo_cmd} "${_update_action}")
	fi
    fi

    pushd ${_proj_dir}
    rm -f ${FLIES_PUBLICAN_LOG}
    if [ -z ${ACTION} ]; then
	if grep -e "brand:.*" publican.cfg ; then
	    # Remove brand
	    echo "    Removing brand."
	    mv publican.cfg publican.cfg.orig
	    sed -e 's/brand:.*//' publican.cfg.orig > publican.cfg
	fi

	if [ ! -d "pot" ]; then
	    echo "    pot does not exist, update_pot now!"
	    ${PUBLICAN_CMD} update_pot >> ${FLIES_PUBLICAN_LOG}
	    touch pot
	fi

	if [ "publican.cfg" -nt "pot" ]; then
	    echo "    "publican.cfg" is newer than "pot", update_pot needed."
	    ${PUBLICAN_CMD} update_pot >> ${FLIES_PUBLICAN_LOG}
	fi

	${PUBLICAN_CMD}  update_po --langs="${LANGS}" >> ${FLIES_PUBLICAN_LOG}

	_proj_name=$(eval echo \$${pProj}_NAME)
	_proj_desc=$(eval echo \$${pProj}_DESC)
    fi

    if [  -z "${ACTION}" -o "${ACTION}" = "createproj" ]; then
	echo "   Creating project ${_proj_name}"
	${FLIES_PUBLICAN_CMD} createproj ${FLIES_PUBLICAN_COMMON_OPTS} --flies "${FLIES_URL}" --proj "${_proj}" --name "${_proj_name}" --desc "${_proj_name}" >> ${FLIES_PUBLICAN_LOG}

	if [ $? -ne 0 ]; then
	    echo "Error occurs, skip following steps!"
	    continue
	fi
    fi

    if [ -z "${ACTION}" -o "${ACTION}" = "createiter" ]; then
	echo "       Creating project iteration as ${INIT_NAME}"
	${FLIES_PUBLICAN_CMD} createiter ${FLIES_PUBLICAN_COMMON_OPTS} --flies "${FLIES_URL}" --proj "${_proj}" --iter "${INIT_ITER}" --name "${INIT_ITER_NAME}" --desc "${INIT_ITER_DESC}" >> ${FLIES_PUBLICAN_LOG}

	if [ $? -ne 0 ]; then
	    echo "Error occurs, skip following steps!"
	    continue
	fi
    fi

    if [ -z "${ACTION}" -o "${ACTION}"="upload" ]; then
	upload_ ${_proj}
    fi

    popd
done
rm -f tmp?.*
echo "Done!"

