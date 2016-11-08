/*
 * jQuery File Upload Plugin JS Example 8.9.1
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/* global $, window, refreshStatistics */

$(function () {
    'use strict';

    $('.fileupload').each(function() {
        var $doc = $(document),
            uploadForm = $(this),
            url = uploadForm.attr('action'),
            dropZone = uploadForm.find('.drag-drop'),
            filesList = uploadForm.find('.files'),
            errorList = uploadForm.find('.js-errors'),
            advancedSettings = uploadForm.find('.js-fileupload-advanced-settings'),
            startButton = uploadForm.find('.js-fileupload-main-start'),
            doneButton = uploadForm.find('.fileupload-done'),
            cancelButton = uploadForm.find('.fileupload-cancel'),
            closeButton = uploadForm.find('.fileupload-close'),
            closeButtons = doneButton.add(cancelButton).add(closeButton),
            container = uploadForm.closest('.modal'),
            revealButtonId = container.attr('id') + '-toggle-button',
            revealButton = $('#' + revealButtonId),
            countIndicator = uploadForm.find('.js-file-count'),
            filePathField = uploadForm.find('input[name=filepath]'),
            fileParamsField = uploadForm.find('textarea[name=fileparams]'),

            // Wraps the i18n function that is attached to the widget options
            // so that it can be used below without a reference to options.
            i18n = function () {
                var widget = uploadForm.data('blueimp-fileupload') || uploadForm.data('fileupload'),
                    options = widget.options;
                return options.i18n.apply(options, arguments);
            },
            showFileCount = (function showFileCount (numberOfFiles) {
                var // options = getOptions(),
                    noFiles = numberOfFiles === 0,
                    message;

                if (noFiles) {
                    message = i18n('jsf.upload.NoDocumentsQueued'); // No documents queued.
                } else if (numberOfFiles === 1) {
                    message = i18n('jsf.upload.OneDocumentQueued'); // 1 document queued.
                } else {
                    message = i18n('jsf.upload.NumberOfDocumentsQueued', { documentCount: numberOfFiles }); // {documentCount} documents queued.
                }
                countIndicator.text(message);

                // start button should only be enabled if there are files to upload
                startButton.attr('disabled', noFiles);
                $('.js-files-panel').toggleClass('is-hidden', noFiles);
            }),
            updateCountIndicator = (function updateCountIndicator (options) {
                var numberOfFiles = options.getNumberOfFiles();
                showFileCount(numberOfFiles);
            }),
            resetUploadForm = (function resetUploadForm () {
                errorList.empty();
                // individual items should clean up any resources they use
                filesList.find('.cancel').click();
                // remove items that don't have a cancel button
                filesList.empty();
                startButton.removeClass('is-hidden')
                           .attr('disabled', true)
                           .text('Upload Documents');
                doneButton.addClass('is-hidden').prop('disabled', true);
                cancelButton.removeClass('is-hidden').prop('disabled', false);
                dropZone.removeClass('is-hidden');
                advancedSettings.removeClass('is-hidden');
                // clear out path and custom attributes
                filePathField.val('');
                fileParamsField.val('');
                $('.js-files-panel').addClass('is-hidden');

                showFileCount(0);
            }),
            updateUploadCountIndicator = (function updateUploadCountIndicator (options) {
                var counts = options.getFileCounts(),
                    message;

                if (counts.failed > 0) {
                    message = i18n('jsf.upload.UploadedOfTotalWithFailures', counts); // Uploaded {uploaded} of {total} files. {failed} uploads failed.
                } else {
                    message = i18n('jsf.upload.UploadedOfTotal', counts); // Uploaded {uploaded} of {total} files.
                }
                countIndicator.text(message);

                if ((counts.uploaded + counts.failed) === counts.total) {
                    startButton.addClass('is-hidden').prop('disabled', true);
                    doneButton.removeClass('is-hidden').prop('disabled', false);
                    cancelButton.addClass('is-hidden').prop('disabled', true);
                    container.off('hide.zanata.modal', confirmCancelUpload);
                    $(window).off('beforeunload', confirmLeavePage);
                }
            });

        // move the container to the end of the body so it is on top of everything
        container.appendTo('body');

        revealButton.bind('click', resetUploadForm);

        closeButtons.bind('click', function (e) {
            container.trigger('hide.zanata.modal');
        });

        container.on('hide.zanata.modal', function () {
            refreshStatistics();
        });

        function confirmCancelUpload () {
            var confirmCancel = confirm(i18n('jsf.upload.ConfirmStopUploading')); // Do you really want to stop uploading files?
            if (confirmCancel) {
                container.off('hide.zanata.modal', confirmCancelUpload);
                $(window).off('beforeunload', confirmLeavePage);
            }
            return confirmCancel;
        }

        function confirmLeavePage () {
            return i18n('jsf.upload.ConfirmInterruptByLeavingPage'); // Do you really want to interrupt your uploading files by leaving this page?
        }

        // prevent default file drop behaviour on the page
        $doc.bind('drop dragover', function (e) {
            e.preventDefault();
        });

        dropZone.bind('drop dragleave dragend', function (e) {
            e.preventDefault();
            dropZone.removeClass('is-active');
        });
        dropZone.bind('dragover', function (e) {
            e.preventDefault();
            dropZone.addClass('is-active');
        });

        var accepted = uploadForm.data('accepted') || '';
        var acceptedTypes = (function (commaSeparated) {
            var types = commaSeparated.split(',');
            $.each(types, function (i, type) {
                types[i] = type.trim();
            });
            return new RegExp('(^|\\.)(' + types.join('|') + ')$', 'i');
        })(accepted);

        if (accepted.trim().length === 0) {
            uploadForm.find('.js-upload-supported').addClass('is-hidden');
        } else {
            uploadForm.find('.js-upload-not-supported').addClass('is-hidden');
        }

        var maxFilesString = uploadForm.data('maxfiles');
        var maxFiles = parseInt(maxFilesString);
        if (typeof maxFiles !== 'number' || isNaN(maxFiles)) {
            maxFiles = 100; // default fallback if it is not properly configured
        }


        var messages = {};
        // get UI strings from the page
        // they are included in the markup of the page so they can easily go
        // through the same translation workflow as other UI strings.
        var messageElement = uploadForm.find('.js-upload-strings');
        messageElement.children().each(function () {
            var item = $(this);
            var key = item.data('key');
            var value = item.text();
            messages[key] = value;
        });


        uploadForm.fileupload({
            url: url,
            container: container,
            confirmCancelUpload: confirmCancelUpload,
            confirmLeavePage: confirmLeavePage,
            sequentialUploads: true,
            maxFileSize: 200*1024*1024,
            maxNumberOfFiles: maxFiles,
            dropZone: dropZone,
            messages: messages,
            advancedSettings: advancedSettings,
            beforeAdd: (function beforeAdd (e, data) {
                errorList.empty();
                startButton.attr('disabled', true);
            }),
            afterAdd: (function afterAdd (e, data) {
                updateCountIndicator(data);
            }),
            errorList: errorList,
            acceptFileTypes: acceptedTypes,
            failed: (function updateCount (e, data) {
                // update count when removing files
                var $this = $(this),
                    that = $this.data('blueimp-fileupload') ||
                           $this.data('fileupload'),
                    options = that.options;
                if (data.errorThrown == 'abort') {
                    updateCountIndicator(options);
                } else {
                    updateUploadCountIndicator(options);
                }
            }),
            completed: (function completed (e, data) {
                var $this = $(this),
                that = $this.data('blueimp-fileupload') ||
                       $this.data('fileupload'),
                options = that.options;
                updateUploadCountIndicator(options);
            }),
            updateUploadCountIndicator: updateUploadCountIndicator
        });

        // FIXME may be unnecessary. If necessary, it could just go in the above options
        // Enable iframe cross-domain access via redirect option:
        uploadForm.fileupload(
            'option',
            'redirect',
            window.location.href.replace(
                /\/[^\/]*$/,
                '/cors/result.html?%s'
            )
        );
    });

});
