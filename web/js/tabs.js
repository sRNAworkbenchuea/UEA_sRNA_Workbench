$(function() {

  var Nav = (function() {
    
    var
      nav     = $('.nav'),
      burger  = $('.burger'),
      page    = $('.page'),
      section = $('.section'),
      link    = nav.find('.nav__link'),
      navH    = nav.innerHeight(),
      isOpen  = true,
      hasT    = false;
    
    var toggleNav = function() {
      nav.toggleClass('nav--active');
      burger.toggleClass('burger--close');
      shiftPage();
    };
    
    var shiftPage = function() {
      if (!isOpen) {
        page.css({
          'transform': 'translateY(' + navH + 'px)'
        });
        isOpen = true;
      } else {
        page.css({
          'transform': 'none'
        });
        isOpen = false;
      }
    };
    
    var switchPage = function(e) {
      var self = $(this);
      var i = self.parents('.nav__item').index();
      var s = section.eq(i);
      var a = $('section.section--active');
      var t = $(e.target);
      
      if (!hasT) {
        if (i == a.index()) {
          return false;
        }
        a
        .addClass('section--hidden')
        .removeClass('section--active');

        s.addClass('section--active');

        hasT = true;

        a.on('transitionend', function() {
          $(this).removeClass('section--hidden');
          hasT = false;
          a.off('transitionend');
        });
      }

      return false;
    };
    
    var keyNav = function(e) {
      var a = $('section.section--active');
      var aNext = a.next();
      var aPrev = a.prev();
      var i = a.index();
      
      
      if (!hasT) {
        if (e.keyCode === 37) {
        
          if (aPrev.length === 0) {
            aPrev = section.last();
          }

          hasT = true;

          aPrev.addClass('section--active');
          a
            .addClass('section--hidden')
            .removeClass('section--active');

          a.on('transitionend', function() {
            a.removeClass('section--hidden');
            hasT = false;
            a.off('transitionend');
          });

        } else if (e.keyCode === 39) {

          if (aNext.length === 0) {
            aNext = section.eq(0)
          } 


          aNext.addClass('section--active');
          a
            .addClass('section--hidden')
            .removeClass('section--active');

          hasT = true;

          aNext.on('transitionend', function() {
            a.removeClass('section--hidden');
            hasT = false;
            aNext.off('transitionend');
          });

        } else {
          return
        }
      }  
    };
      
    var bindActions = function() {
      burger.on('click', toggleNav);
      link.on('click', switchPage);
      $(document).on('ready', function() {
         page.css({
          'transform': 'translateY(' + navH + 'px)'
        });
      });
      $('body').on('keydown', keyNav);
    };
    
    var init = function() {
      bindActions();
    };
    
    return {
      init: init
    };
    
  }());

  Nav.init();

});

